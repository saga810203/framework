package org.jfw.apt.model.orm;

import java.util.List;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.SelectList;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.orm.core.OrmHandler;

public class SelectListOperateCG extends DBOperateCG {

	private String querySql;
	private String realReturnType;
	private PersistentObject bean;

	private OrmHandler[] fieldHandlers;

	private OrmHandler[] whereHandlers;

	private String otherSentence;
	private SelectList selectList;

	private void checkReturnType() throws AptException {

		if (this.returnType.startsWith("java.util.List<") && this.returnType.endsWith(">")) {
			this.realReturnType = this.returnType.substring(15);
			this.realReturnType = this.realReturnType.substring(0, this.realReturnType.length() - 1).trim();
		} else {
			throw new AptException(this.ref, "this method must be return java.util.List<Object>");
		}
		String bcn = this.realReturnType;
		this.bean = this.ormDefine.getPersistentObject(bcn);
		if (this.bean == null)
			throw new AptException(ref, "this mehtod return type not is a persistentObject  in this project");

		PersistentObjectKind kind = this.bean.getKind();
		if (kind == PersistentObjectKind.VIEW || kind == PersistentObjectKind.EXTEND_VIEW
				|| kind == PersistentObjectKind.VIRTUAL_TABLE) {
			throw new AptException(ref,
					"this mehtod return type not is a  invalid persistentObject(kind = TABLE or EXTEND_TABLE)");
		}

		List<Column> list = this.bean.getAllColumn();
		this.fieldHandlers = new OrmHandler[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			try {
				this.fieldHandlers[i] = (OrmHandler) list.get(i).getDataElement().getHandlerClass().newInstance();
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

		if (this.otherSentence != null) {
			sb.append(" ");
			Utils.addSqlToStringBuilder(this.otherSentence, sb);
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
		this.selectList = this.ref.getAnnotation(SelectList.class);
		if (this.selectList == null)
			throw new AptException(this.ref, "nofound @SelectList on this method");
		this.otherSentence = Utils.emptyToNull(selectList.value());
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
		String rt = this.realReturnType;
		
			sb.append("java.util.List<").append(rt).append("> result = new java.util.ArrayList<").append(rt)
					.append(">();\r\n");
			sb.append("while(rs.next()){");
			
				sb.append(this.realReturnType).append(" obj = new ").append(this.realReturnType).append("();");
				List<Column> list = this.bean.getAllColumn();
				for (int i = 0; i < this.fieldHandlers.length; ++i) {
					this.fieldHandlers[i].readValue(sb, "obj." + list.get(i).getSetter() + "(", ");", i + 1,
							list.get(i).isNullable(), this.attributes);
				}

				sb.append("result.add(obj);");
			

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
