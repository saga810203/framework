package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import org.jfw.apt.annotation.orm.Insert;
import org.jfw.apt.exception.AptException;

public class InsertOperateCG extends DBOperateCG {

	private Insert insert;
	private PersistentObject po;
	private List<Column> columns = new ArrayList<Column>();

	@Override
	protected void prepare() throws AptException {
		this.insert = this.ref.getAnnotation(Insert.class);
		if (this.insert == null)
			throw new AptException(this.ref, "nofound @Insert on this method");
		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@Insert) must return int");

		if (this.params.size() != 2)
			throw new AptException(ref, "this method(@Insert) parameters count must be 2");

		this.po = this.ormDefine.getPersistentObject(this.params.get(1).getTypeName());
		if (this.po == null)
			throw new AptException(ref, "this method(@Insert) second parameter must be a PersistentObject");
		if (this.po.getKind() != PersistentObjectKind.TABLE)
			throw new AptException(ref,
					"this method(@Insert) second parameter must be a PersistentObject(kind == TABLE)");
		this.columns = po.getInsertColumn();
		this.initOrmHandlers();
		this.buildStaticSQL();

	}

	private void initOrmHandlers() throws AptException {
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (null != col.getFixInsertSqlValue())
				continue;
			col.initHandler(ref);
			col.getHandler().init(this.params.get(1).getName() + "." + col.getGetter() + "()", true, col.nullable,
					this.attributes);
			col.getHandler().prepare(sb);

		}

	}

	private void buildStaticSQL() {
		this.sb.append("String sql=\"INSERT INTO ").append(this.po.getFromSentence()).append(" (");

		boolean firstColumn = true;
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (firstColumn) {
				firstColumn = false;
			} else {
				sb.append(",");
			}
			sb.append(col.getDbName());
		}
		sb.append(") values (");
		firstColumn = true;
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (firstColumn) {
				firstColumn = false;
			} else {
				sb.append(",");
			}
			String value = col.getFixInsertSqlValue();
			if (value == null) {
				sb.append("?");
			} else {
				sb.append(value);
			}
		}
		sb.append(")\";\r\n");
	}

	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (null == col.getFixInsertSqlValue()) {
				col.getHandler().writeValue(sb, false);
			}
		}
	}

	@Override
	protected void buildHandleResult() {
		sb.append("return ps.executeUpdate();");

	}

	@Override
	protected boolean needRelaceResource() {
		for (Column col : this.columns) {
			if (null == col.getHandler())
				continue;
			if (col.getHandler().isReplaceResource())
				return true;
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		for (Column col : this.columns) {
			if (null == col.getHandler())
				continue;
			col.getHandler().replaceResource(sb);
		}
	}

}
