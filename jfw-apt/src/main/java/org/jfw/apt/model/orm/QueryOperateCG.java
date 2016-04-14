package org.jfw.apt.model.orm;

import java.util.List;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.Query;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.orm.core.enums.DataElement;

public class QueryOperateCG extends DBOperateCG {

	private String querySql;
	private String realReturnType;
	private PersistentObject bean;

	private OrmHandler[] fieldHandlers;

	private WhereSentence where;

	// private boolean singleRow() default false;
	// DataElement singleColumn() default DataElement.invalid_de;
	// Class<?> resultClass() default Object.class;
	// String otherSentence() default "";
	// Where where() default @Where();
	//
	private Query query;

	private void checkReturnType() throws AptException {
		if (this.query.singleRow()) {
			this.realReturnType = this.returnType;
		} else {
			if (this.returnType.startsWith("java.util.List<") && this.returnType.endsWith(">")) {
				this.realReturnType = this.returnType.substring(15);
				this.realReturnType = this.realReturnType.substring(0, this.realReturnType.length() - 1).trim();
			} else {
				throw new AptException(this.ref, "this method must be return java.util.List<Object>");
			}
		}
		if (this.query.singleColumn() == DataElement.invalid_de) {
			String bcn = this.realReturnType;
			this.bean = this.ormDefine.getPersistentObject(bcn);
			if (this.bean == null)
				throw new AptException(ref, "this mehtod return type not is a persistentObject  in this project");

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

		} else {
			String sQueryType = this.getSingleQueryType();
			if (this.query.singleRow()) {
				if (!this.realReturnType.equals(sQueryType))
					throw new AptException(this.ref,
							"return type must equals ormhandler.supportedClass in this method");
			} else {
				if (Utils.isPrimitive(sQueryType)) {
					sQueryType = Utils.getWrapClass(sQueryType);
					if (!this.realReturnType.equals(sQueryType))
						throw new AptException(this.ref,
								"return type must equals ormhandler.supportedClass boxedClass in this method");
				} else {
					if (!this.realReturnType.equals(sQueryType))
						throw new AptException(this.ref,
								"return type must equals ormhandler.supportedClass in this method");
				}
			}
			this.fieldHandlers = new OrmHandler[1];
			try {
				this.fieldHandlers[0] = (OrmHandler) this.query.singleColumn().getHandlerClass().newInstance();
			} catch (Exception ee) {
				String m = ee.getMessage();
				throw new AptException(ref, "can't create ormHandler instance:" + m == null ? "" : m);
			}
		}
	}

	private String getSingleQueryType() throws AptException {
		try {
			return Utils.getSupportedClassName(this.query.singleColumn().getHandlerClass());
		} catch (Exception e) {
			throw new AptException(this.ref,
					"error @Query.singleColumn().getHandlerClass().newInstance().getSupportedClass()");
		}
	}

	private void buildStaticSQL() throws AptException {
		sb.append("String sql = \"");
		Utils.addSqlToStringBuilder(this.querySql, sb);
		String whereSql = this.where.getStaticSQL();
		if (whereSql != null && whereSql.trim().length() > 0) {
			sb.append(" WHERE ");
			Utils.addSqlToStringBuilder(whereSql, sb);
		}
		sb.append("\";\r\n");
	}

	private void buildDynamicSQL() throws AptException {
		sb.append("StringBuilder sql = new StringBuilder();\r\n");
		this.where.appendToSql(sb);
	}

	private void checkSQL() throws AptException {
		if (this.bean == null) {
			this.querySql = this.query.singleColumnSql();
			if (this.querySql == null || this.querySql.trim().length() == 0) {
				throw new AptException(this.ref, "not found singleColumnSql in @Query");
			}
		} else {
			this.querySql = "SELECT " + this.bean.getQueryFields() + " FROM ";
			String fs = this.bean.getFromSentence();
			if (fs == null || fs.trim().length() == 0)
				throw new AptException(this.ref, "unknow fromsentence");
			this.querySql = this.querySql + fs.trim();
		}
	}

	@Override
	protected void prepare() throws AptException {
		this.query = this.ref.getAnnotation(Query.class);
		if (this.query == null)
			throw new AptException(this.ref, "nofound @Query on this method");
		this.checkReturnType();
		this.where = WhereSentence.build(ref, this.query.where(), this.attributes);
		this.dynamic = where.isDynamicWhereSql();
//		this.checkReturnType();
		this.checkSQL();
		where.prepare(sb);		
		if (this.dynamic) {
			this.buildDynamicSQL();
		} else {
			this.buildStaticSQL();
		}

	}

	@Override
	protected void buildSqlParamter() {		
		this.where.buildParam(sb);
		sb.append("java.sql.ResultSet rs = ps.executeQuery();\r\n");
	}

	@Override
	protected void buildHandleResult() {
		sb.append("try{\r\n");
		String rt = this.realReturnType;
		if (!this.query.singleRow()) {

			if (Utils.isPrimitive(rt))
				rt = Utils.getWrapClass(rt);
			sb.append("java.util.List<").append(rt).append("> result = new java.util。ArrayList<").append(rt)
					.append(">();\r\n");
			sb.append("while(rs.next(){");
			if (this.query.singleColumn() != DataElement.invalid_de) {
				this.fieldHandlers[0].readValue(sb, "result.add(", ");", 1, this.query.singleColumn().isNullable(),
						this.attributes);
			} else {
				sb.append(this.realReturnType).append(" obj = new ").append(this.realReturnType).append("();");
				List<Column> list = this.bean.getAllColumn();
				for (int i = 0; i < this.fieldHandlers.length; ++i) {
					this.fieldHandlers[i].readValue(sb, "obj." + list.get(i).getSetter() + "(", ");", i + 1,
							list.get(i).isNullable(), this.attributes);
				}

				sb.append("result.add(obj);");
			}

			sb.append("}");

		} else {
			sb.append(this.realReturnType).append(" result ");
			if (!Utils.isPrimitive(rt))
				sb.append("= null");
			sb.append(";\r\n");
			sb.append("if(rs.next()){");
			if (this.query.singleColumn() != DataElement.invalid_de) {
				this.fieldHandlers[0].readValue(sb, "result = ", ";", 1, this.query.singleColumn().isNullable(),
						this.attributes);
			} else {
				sb.append("result = new ").append(this.realReturnType).append("();");

				List<Column> list = this.bean.getAllColumn();
				for (int i = 0; i < this.fieldHandlers.length; ++i) {
					this.fieldHandlers[i].readValue(sb, "result." + list.get(i).getSetter() + "(", ");", i + 1,
							list.get(i).isNullable(), this.attributes);
				}
			}
			sb.append("}");
		}
		sb.append("return result;\r\n");
		sb.append("}finally{try{rs.close();}catch(Exception e){}}");

	}

	@Override
	protected boolean needRelaceResource() {
		return this.where.needReplaceResource();
	}

	@Override
	protected void relaceResource() {
		this.where.replaceResource(sb);
		
	}

}
