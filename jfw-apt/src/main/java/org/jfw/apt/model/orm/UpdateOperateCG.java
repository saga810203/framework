package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.Update;
import org.jfw.apt.exception.AptException;

public class UpdateOperateCG extends DBOperateCG {

	private Update update;
	private PersistentObject po;
	private String[] wheres;
	private List<Column> columns;
	private List<Column> values = new ArrayList<Column>();
	private List<Column> filters = new ArrayList<Column>();

	@Override
	protected void prepare() throws AptException {
		this.update = this.ref.getAnnotation(Update.class);
		if (this.update == null)
			throw new AptException(this.ref, "nofound @Update on this method");
		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@Update) must return int");

		if (this.params.size() != 2)
			throw new AptException(ref, "this method(@Update) parameters count must be 2");

		this.po = this.ormDefine.getPersistentObject(this.params.get(1).getTypeName());
		if (this.po == null)
			throw new AptException(ref, "this method(@Update) second parameter must be a PersistentObject");
		if (this.po.getKind() != PersistentObjectKind.TABLE)
			throw new AptException(ref,
					"this method(@Update) second parameter must be a PersistentObject(kind == TABLE)");
		this.columns = po.getAllColumn();
		String unName = Utils.emptyToNull(update.value());
		if (unName == null)
			throw new AptException(ref, "@Update must be has name");
		if (unName.equals("PrimaryKey")) {
			if (po.getPrimaryKey() == null)
				throw new AptException(ref,
						"this method(@Update) second parameter must be a PersistentObject(has primaryKey)");
			this.wheres = po.getPrimaryKey().getJavaNames();
		} else {
			UniqueConstraint uc = po.getUniqueConstraint(unName);
			if (uc == null)
				throw new AptException(ref,
						"this method(@Update) second parameter must be a PersistentObject(has @unique'name=" + unName
								+ ")");
			this.wheres = uc.getJavaNames();
		}
		this.splitColumns();
		this.initOrmHandlers();
		this.dynamic = this.update.dynamicValue();
		if (this.dynamic) {
			this.sortColumns(this.filters);
			this.sortColumns(this.values);
			this.buildDynamicSQL();
		} else {
			this.buildStaticSQL();
		}

	}

	private void buildDynamicSQL() {
		this.sb.append("StringBuilder sql = new StringBuilder();\r\n");
		this.sb.append("sql.append(\"UPDATE ").append(this.po.getFromSentence()).append(" SET ");
		String numOfUpdateColumns = null;
		boolean isFirstDynamicValue = true;
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			String fixVal = col.getFixValueWithUpdate();
			if (fixVal != null) {
				if (i != 0)
					sb.append(",");
				sb.append(col.getDbName()).append("=");
				Utils.addSqlToStringBuilder(fixVal, sb);
			} else if (col.isPrimitive()) {
				if (i != 0)
					sb.append(",");
				sb.append(col.getDbName()).append("=");
				sb.append("?");
			} else {
				if (isFirstDynamicValue) {
					isFirstDynamicValue = true;
					sb.append("\");\r\n");
				}
				if(i==0){
					numOfUpdateColumns = this.getTempalteVariableName();
					sb.append(" int ").append(numOfUpdateColumns).append(" = 0 ;\r\n");
				}
				
				
				sb.append("if(");
				col.getHandler().checkNull(sb);
				sb.append("){\r\n");
				if(null==numOfUpdateColumns){
					sb.append("sql.append(\",").append(col.getDbName()).append("=?\");\r\n}\r\n");
				}else{
					sb.append("++").append(numOfUpdateColumns).append(";\r\nif(")
					.append(numOfUpdateColumns).append(" > 1){\r\nsql.append(\",\");\r\n}\r\n");
					sb.append("sql.append(\"").append(col.getDbName()).append("=?\");\r\n}\r\n");					
				}
			}
		}
		if(numOfUpdateColumns!=null){
			sb.append("if(0==").append(numOfUpdateColumns).append(") return 0;\r\n");
		}
		for (int i = 0; i < this.filters.size(); ++i) {
			if (i == 0) {
				sb.append("sql.append(\" WHERE ");
			} else {
				sb.append(" AND ");
			}
			Column col = this.filters.get(i);
			sb.append(col.getDbName()).append("=?");
		}
		sb.append("\");\r\n");

	}

	private void initOrmHandlers() throws AptException {
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if (null != col.getFixValueWithUpdate())
				continue;
			col.initHandler(ref);
			if (this.update.dynamicValue()) {
				col.getHandler().init(this.params.get(1).getName() + "." + col.getGetter() + "()", true, true,
						this.attributes);
			} else {
				col.getHandler().init(this.params.get(1).getName() + "." + col.getGetter() + "()", true, col.nullable,
						this.attributes);
			}
			col.getHandler().prepare(sb);

		}
		for (int i = 0; i < this.filters.size(); ++i) {
			Column col = this.filters.get(i);
			col.initHandler(ref);
			col.getHandler().init(this.params.get(1).getName() + "." + col.getGetter() + "()", true, false,
					this.attributes);
			col.getHandler().prepare(sb);
		}

	}

	private void splitColumns() throws AptException {
		for (Column col : this.columns) {
			boolean inWhere = false;
			for (String s : this.wheres) {
				if (s.equals(col.getJavaName())) {
					this.filters.add(col);
					inWhere = true;
					break;
				}
			}
			if ((!inWhere) && col.getDataElement().supportedUpdate())
				this.values.add(col);
		}
		if (this.values.isEmpty())
			throw new AptException(this.ref, "not found modify value");
	}

	private void buildStaticSQL() {
		this.sb.append("String sql=\"UPDATE ").append(this.po.getFromSentence()).append(" SET");
		for (int i = 0; i < this.values.size(); ++i) {
			sb.append(i == 0 ? " " : ",");
			Column col = this.values.get(i);
			String value = Utils.emptyToNull(col.getDataElement().getFixSqlValueWithUpdate());

			sb.append(this.values.get(i).getDbName()).append("=").append(null == value ? "?" : value);
		}
		sb.append(" WHERE");
		for (int i = 0; i < this.filters.size(); ++i) {
			sb.append(i == 0 ? " " : " AND ").append(this.filters.get(i).getDbName()).append("=?");
		}
		sb.append("\";\r\n");

	}

	private void sortColumns(List<Column> list) {
		Collections.sort(list, new Comparator<Column>() {

			private int getSortedNum(Column col) {
				String fixUpdate = Utils.emptyToNull(col.getDataElement().getFixSqlValueWithUpdate());
				if (null != fixUpdate)
					return 0;
				if (col.isPrimitive())
					return 1;
				return 2;
			}

			@Override
			public int compare(Column o1, Column o2) {
				return getSortedNum(o1) - getSortedNum(o2);
			}

		});
	}

	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if (null == col.getFixValueWithUpdate()) {
				col.getHandler().writeValue(sb,true);
			}
		}
		for (int i = 0; i < this.filters.size(); ++i) {
			this.filters.get(i).getHandler().writeValue(sb,false);
		}

	}

	@Override
	protected void buildHandleResult() {
		sb.append("return ps.executeUpdate();");

	}

	@Override
	protected boolean needRelaceResource() {
		for (Column col : this.values) {
			if(null==col.getHandler()) continue;
			if (col.getHandler().isReplaceResource())
				return true;
		}
		for (Column col : this.filters) {
			if (col.getHandler().isReplaceResource())
				return true;
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		for (Column col : this.values) {
			if(null==col.getHandler())continue;
			col.getHandler().replaceResource(sb);
		}
		for (Column col : this.filters) {
			col.getHandler().replaceResource(sb);
		}

	}

}
