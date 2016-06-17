package org.jfw.apt.model.orm;

import java.util.List;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.SelectOne;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.orm.core.OrmHandler;

public class SelectOneOperateCG  extends DBOperateCG {

	private String querySql;
	private PersistentObject bean;

	private OrmHandler[] fieldHandlers;
	private Column[] queryColumns;

	private OrmHandler[] whereHandlers;

	private SelectOne selectOne;

	private void checkReturnType() throws AptException {


		String bcn = this.returnType;
		this.bean = this.ormDefine.getPersistentObject(bcn);
		if (this.bean == null)
			throw new AptException(ref, "this mehtod return type not is a persistentObject  in this project");

		PersistentObjectKind kind = this.bean.getKind();
		if (kind == PersistentObjectKind.VIEW || kind == PersistentObjectKind.EXTEND_VIEW
				|| kind == PersistentObjectKind.VIRTUAL_TABLE) {
			throw new AptException(ref,
					"this mehtod return type not is a  invalid persistentObject(kind = TABLE or EXTEND_TABLE)");
		}

		List<Column> list = this.bean.getQueryColumn();
		this.queryColumns = new Column[list.size()];
		this.fieldHandlers = new OrmHandler[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			this.queryColumns[i] = list.get(i);
			try {
				this.fieldHandlers[i] = (OrmHandler) this.queryColumns[i].getDataElement().getHandlerClass().newInstance();
			} catch (Exception ee) {
				String m = ee.getMessage();
				throw new AptException(ref, "can't create ormHandler instance:" + m == null ? "" : m);
			}
		}

	}

	private void buildStaticSQL() throws AptException {
		this.whereHandlers = new OrmHandler[this.params.size() - 1];

		List<Column> cols = this.bean.getAllColumn();

		sb.append("String sql = \"");
		Utils.addSqlToStringBuilder(this.querySql, sb);

		for (int i = 1; i < this.params.size(); ++i) {
			MethodParamEntry mpe = this.params.get(i);
			String name = mpe.getName();
			boolean found = false;
			for (Column col : cols) {
				if (!name.equals(col.getJavaName()))
					continue;
				if (!col.getClass().equals(Column.class)) {
					throw new AptException(mpe.getRef(),
							"param " + name + " is not a simple column in persistendObject");
				}

				found = true;

				if (1 != i)
					sb.append(" AND ");
				else
					sb.append(" WHERE ");
				sb.append(col.getDbName()).append("=?");

				try {
					this.whereHandlers[i - 1] = (OrmHandler) col.getDataElement().getHandlerClass().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					String m = e.getMessage();
					throw new AptException(mpe.getRef(), "can't create ormHandler instance:" + m == null ? "" : m);
				}
				this.whereHandlers[i - 1].init(name, false, false, this.attributes);
			}
			if (!found) {
				throw new AptException(mpe.getRef(), "param " + name + " is not a column in persistendObject");
			}
		}
		sb.append("\";\r\n");
	}

	private void checkSQL() throws AptException {
		this.querySql = "SELECT " + this.bean.getQueryFields() + " FROM ";
		String fs = this.bean.getFromSentence();
		if (fs == null || fs.trim().length() == 0)
			throw new AptException(this.ref, "unknow fromsentence");
		this.querySql = this.querySql + fs.trim();
	}

	@Override
	protected void prepare() throws AptException {
		this.selectOne = this.ref.getAnnotation(SelectOne.class);
		if (this.selectOne == null)
			throw new AptException(this.ref, "nofound @SelectOne on this method");
		this.checkReturnType();
		this.checkSQL();
		this.buildStaticSQL();

		for (int i = 0; i < this.whereHandlers.length - 1; ++i) {
			this.whereHandlers[i].prepare(sb);
		}

	}

	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.whereHandlers.length; ++i) {
			this.whereHandlers[i].writeValue(sb, false);
		}
		sb.append("java.sql.ResultSet rs = ps.executeQuery();\r\n");
	}

	@Override
	protected void buildHandleResult() {
		sb.append("try{\r\n");
		String rt = this.returnType;
		
			sb.append(rt).append(" result = null;\r\n");
			sb.append("if(rs.next()){");
			
				sb.append(" result = new ").append(this.returnType).append("();");
				for (int i = 0; i < this.fieldHandlers.length; ++i) {
					this.fieldHandlers[i].readValue(sb, "result." + this.queryColumns[i].getSetter() + "(", ");", i + 1,
							this.queryColumns[i].isNullable(), this.attributes);
				}
			sb.append("}");

		
		sb.append("return result;\r\n");
		sb.append("}finally{try{rs.close();}catch(Exception e){}}");

	}

	@Override
	protected boolean needRelaceResource() {
		for (int i = 0; i < this.whereHandlers.length; ++i) {
			if (this.whereHandlers[i].isReplaceResource())
				return true;
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		if(this.needRelaceResource())
		for (int i = 0; i < this.whereHandlers.length; ++i) {
			if (this.whereHandlers[i].isReplaceResource())
				this.whereHandlers[i].replaceResource(sb);
		}

	}
}
