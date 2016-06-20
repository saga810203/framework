package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.UpdateWith;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;

public class UpdateWithOperateCG extends DBOperateCG {
	private List<Column> where = new ArrayList<Column>();
	private List<Column> values = new ArrayList<Column>();
	private List<Column> columns;
	private List<String> colNames = new ArrayList<String>();
	private UpdateWith updateWith;
	private List<String> whereColNames = new ArrayList<String>();
	private PersistentObject po;
	private List<String> fixValColumns = new ArrayList<String>();
	private List<String> includeFixValColumn = new ArrayList<String>();

	private String fixSetSQL;

	private void initIncludeFixValueColumn() {
		String[] ec = this.updateWith.includeFixValueColumn();
		if (ec == null)
			return;
		for (String str : ec) {
			if (str != null && str.trim().length() > 0)
				this.includeFixValColumn.add(str.trim());
		}
	}

	private void resolerUpdateColumns() throws AptException {
		for (int i = 1; i < this.params.size(); ++i) {
			String pn = this.params.get(i).getName();
			boolean found = false;
			for (Column col : this.columns) {
				if (col.getJavaName().equals(pn)) {
					found = true;
					if (this.whereColNames.contains(pn))
						this.where.add(col);
					else
						this.values.add(col);
					col.initHandler(ref);
					col.getHandler().init(pn, false, false, this.attributes);
				}
			}
			if (!found) {
				throw new AptException(ref, "param[" + pn + "] not found in persistentObject attributes");
			}
		}

		if (!this.includeFixValColumn.isEmpty()) {
			for (Column col : this.columns) {
				if (this.where.contains(col))
					continue;
				if (this.values.contains(col))
					continue;
				if ((col.getFixUpdateSqlValue() != null) && this.includeFixValColumn.contains(col.getJavaName())) {
					this.values.add(col);
					this.fixValColumns.add(col.getJavaName());
				}
			}
		}
		if (this.values.isEmpty() && (null == this.fixSetSQL))
			throw new AptException(ref, "not found change value is define");
	}

	@Override
	protected void prepare() throws AptException {
		this.updateWith = this.ref.getAnnotation(UpdateWith.class);
		if (null == this.updateWith)
			throw new AptException(this.ref, "nofound @UpdateWith on this method");

		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@UpdateWith) must return int");
		this.initIncludeFixValueColumn();
		this.fixSetSQL = Utils.emptyToNull(this.updateWith.value());
		String targetClassName = null;

		try {
			targetClassName = this.updateWith.target().getName();
		} catch (MirroredTypeException e) {
			targetClassName = TypeName.get(e.getTypeMirror()).toString();
		}
		this.po = ormDefine.getPersistentObject(targetClassName);
		if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
			throw new AptException(ref, "this method[@UpdateWith'target value must be Object.class or Class(@Table)");
		}
		String[] tmpArray = this.updateWith.where();
		if (tmpArray != null && tmpArray.length > 0) {
			for (String str : tmpArray) {
				if (str != null && str.trim().length() > 0)
					this.whereColNames.add(str.trim());
			}
		}
		this.columns = this.po.getAllColumn();
		for (ListIterator<Column> it = this.columns.listIterator(); it.hasNext();) {
			this.colNames.add(it.next().getJavaName());
		}
		this.resolerUpdateColumns();

		this.sb.append("String sql=\"UPDATE ").append(this.po.getFromSentence()).append(" SET");
		for (int i = 0; i < this.values.size(); ++i) {
			sb.append(i != 0 ?",": (this.fixSetSQL==null?" ":(" "+this.fixSetSQL+",")));
			Column col = this.values.get(i);
			String value = col.getFixUpdateSqlValue();
			if (value == null) {
				value = "?";
			} else {
				if (!this.fixValColumns.contains(col.getJavaName())) {
					value = "?";
				}
			}

			sb.append(this.values.get(i).getDbName()).append("=").append(value);
		}
		for (int i = 0; i < this.where.size(); ++i) {
			sb.append(i == 0 ? " WHERE " : " AND ").append(this.where.get(i).getDbName()).append("=?");
		}
		sb.append("\";\r\n");
	}

	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if ((col.getFixUpdateSqlValue() != null) && this.fixValColumns.contains(col.getJavaName()))
				continue;
			col.getHandler().writeValue(sb, false);
		}
		for (int i = 0; i < this.where.size(); ++i) {
			this.where.get(i).getHandler().writeValue(sb, false);
		}
	}

	@Override
	protected boolean needRelaceResource() {
		for (Column col : this.values) {
			if ((col.getFixUpdateSqlValue() != null) && this.fixValColumns.contains(col.getJavaName()))
				continue;
			if (col.getHandler().isReplaceResource())
				return true;
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		for (Column col : this.values) {
			if ((col.getFixUpdateSqlValue() != null) && this.fixValColumns.contains(col.getJavaName()))
				continue;
			if (col.getHandler().isReplaceResource())
				col.getHandler().replaceResource(sb);
			;
		}
	}

	@Override
	protected void buildHandleResult() {
		sb.append("return ps.executeUpdate();");
	}

}
