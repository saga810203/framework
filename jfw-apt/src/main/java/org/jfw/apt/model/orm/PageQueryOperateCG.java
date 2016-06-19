package org.jfw.apt.model.orm;

import java.util.List;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.PageQuery;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.orm.core.OrmHandler;

public class PageQueryOperateCG extends DBOperateCG {

	private static final String prefix = "org.jfw.util.PageQueryResult<";
	private static final int prefixLen = prefix.length();
	private String staticWhereSql;
	private String realReturnType;
	private PersistentObject bean;

	private OrmHandler[] fieldHandlers;
	private Column[] queryColumns;

	private WhereSentence where;

	private String otherSentence;
	private PageQuery query;

	private void checkMethod() throws AptException {
		if (this.returnType.startsWith(prefix) && this.returnType.endsWith(">")) {
			this.realReturnType = this.returnType.substring(prefixLen);
			this.realReturnType = this.realReturnType.substring(0, this.realReturnType.length() - 1).trim();
		} else {
			throw new AptException(this.ref, "this method must be return " + prefix + "Object>");
		}

		boolean hasPageSize = false;
		boolean hasPageNo = false;
		for (MethodParamEntry mpe : this.params) {
			if (mpe.getName().equals("pageSize") && mpe.getTypeName().equals("int"))
				hasPageSize = true;
			if (mpe.getName().equals("pageNo") && mpe.getTypeName().equals("int"))
				hasPageNo = true;
		}
		if (!hasPageNo) {
			throw new AptException(this.ref, "this method must has param {int pageNo}");
		}
		if (!hasPageSize) {
			throw new AptException(this.ref, "this method must has param {int pageSize}");
		}
		String bcn = this.realReturnType;
		this.bean = this.ormDefine.getPersistentObject(bcn);
		if (this.bean == null)
			throw new AptException(ref, "this mehtod return type not is a persistentObject  in this project");

		List<Column> list = this.bean.getQueryColumn();
		this.fieldHandlers = new OrmHandler[list.size()];
		this.queryColumns = new Column[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			try {
				this.queryColumns[i] = list.get(i);
				this.fieldHandlers[i] = (OrmHandler) list.get(i).getDataElement().getHandlerClass().newInstance();
			} catch (Exception ee) {
				String m = ee.getMessage();
				throw new AptException(ref, "can't create ormHandler instance:" + m == null ? "" : m);
			}
		}
	}

	private void buildStaticWhereSQL() throws AptException {
		this.staticWhereSql = Utils.emptyToNull(this.where.getStaticSQL());

		if (this.staticWhereSql != null) {
			sb.append("String sql = \"");
			Utils.addSqlToStringBuilder(staticWhereSql, sb);
			sb.append("\";\r\n");
		}
	}

	private void buildDynamicWhereSQL() throws AptException {
		sb.append("StringBuilder sql = new StringBuilder();\r\n");
		this.where.appendToSql(sb);
	}

	private void checkSQL() throws AptException {
		String fs = this.bean.getFromSentence();
		if (fs == null || fs.trim().length() == 0)
			throw new AptException(this.ref, "unknow fromsentence");
	}

	@Override
	protected void prepare() throws AptException {
		this.query = this.ref.getAnnotation(PageQuery.class);
		if (this.query == null)
			throw new AptException(this.ref, "nofound @PageQuery on this method");
		this.otherSentence = Utils.emptyToNull(query.otherSentence());
		this.checkMethod();
		this.where = WhereSentence.build(ref, this.query.where(), this.attributes);
		this.dynamic = where.isDynamicWhereSql();
		this.checkSQL();
		where.prepare(sb);
		if (this.dynamic) {
			this.buildDynamicWhereSQL();
		} else {
			this.buildStaticWhereSQL();
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
		sb.append("java.util.List<").append(rt).append("> _pList = new java.util.ArrayList<").append(rt)
				.append(">();\r\n");

		sb.append("result.setData(_pList);");
		sb.append("int _num4Row = 0;");
		sb.append("while(rs.next()){");

		sb.append(this.realReturnType).append(" obj = new ").append(this.realReturnType).append("();");
		for (int i = 0; i < this.fieldHandlers.length; ++i) {
			this.fieldHandlers[i].readValue(sb, "obj." + this.queryColumns[i].getSetter() + "(", ");", i + 1,
					this.queryColumns[i].isNullable(), this.attributes);
		}

		sb.append("_pList.add(obj);").append("++ _num4Row;if(_num4Row == pageSize) break;");

		sb.append("}");

		sb.append("return result;\r\n");
		sb.append("}finally{try{rs.close();}catch(Exception e){}}");

	}

	protected void forPageQueryWithFirstPage() {
		if (this.dynamic) {
			sb.append("sql = new StringBuilder();").append("sql.append(\"SELECT ").append(this.bean.getQueryFields())
					.append(" FROM ").append(this.bean.getFromSentence()).append("\");");

			sb.append("if(_tmpsql.length()>0){sql.append(_tmpsql);}");
			if (this.otherSentence != null) {
				sb.append("sql.append(\" ");
				Utils.addSqlToStringBuilder(this.otherSentence, sb);
				sb.append("\");");
			}
		} else {
			if (this.staticWhereSql == null) {
				sb.append("sql =\"SELECT ").append(this.bean.getQueryFields()).append(" FROM ")
						.append(this.bean.getFromSentence());
				if (this.otherSentence != null) {
					sb.append(" ");
					Utils.addSqlToStringBuilder(this.otherSentence, sb);
				}
				sb.append("\";\r\n");
			} else {
				sb.append("sql =\"SELECT ").append(this.bean.getQueryFields()).append(" FROM ")
						.append(this.bean.getFromSentence()).append(" \"+").append("_tmpsql");

				if (this.otherSentence != null) {
					sb.append(" +\" ");
					Utils.addSqlToStringBuilder(this.otherSentence, sb);
					sb.append("\";");
				} else {
					sb.append(";\r\n");
				}
			}
		}
	}

	protected void forPageQueryWithNotFirstPage() {

		sb.append(" int _lastPage =_total / pageSize ;if(_total % pageSize !=0){ ++ _lastPage;}");
		sb.append("if(pageNo> _lastPage){pageNo = _lastPage;} result.setPageNo(pageNo);");
		sb.append("--pageNo;int _beginIndex = (pageNo) * pageSize + 1;\r\n")
				.append("int _endIndex = _beginIndex + pageSize;\r\n");

		if (this.dynamic) {
			sb.append("sql = new StringBuilder();").append("sql.append(\"SELECT * FROM ( SELECT ")
					.append(this.bean.getQueryFields()).append(",ROWNUM ROW$NUM FROM ")
					.append(this.bean.getFromSentence()).append("\");");

			sb.append("if(_tmpsql.length()>0){sql.append(_tmpsql);}");
			if (this.otherSentence != null) {
				sb.append("sql.append(\" ");
				Utils.addSqlToStringBuilder(this.otherSentence, sb);
				sb.append("\");");
			}
			sb.append(
					" ) WHERE ROW$NUM >=\").append(_beginIndex).append(\" AND ROW$NUM <\").append(_endIndex).append(\" ORDER BY ROW$NUM\");");
		} else {
			if (this.staticWhereSql == null) {
				sb.append("sql =\"SELECT * FROM ( SELECT ").append(this.bean.getQueryFields())
						.append(",ROWNUM ROW$NUM FROM ").append(this.bean.getFromSentence());
				if (this.otherSentence != null) {
					sb.append(" ");
					Utils.addSqlToStringBuilder(this.otherSentence, sb);
				}
				sb.append(
						") WHERE ROW$NUM >=\"+ _beginIndex +\" AND ROW$NUM <\"+ _endIndex+\" ORDER BY ROW$NUM\";\r\n");
			} else {
				sb.append("sql =\"SELECT * FROM ( SELECT ").append(this.bean.getQueryFields())
						.append(",ROWNUM ROW$NUM FROM ").append(this.bean.getFromSentence()).append(" \"+")
						.append("_tmpsql");

				if (this.otherSentence != null) {
					sb.append(" +\" ");
					Utils.addSqlToStringBuilder(this.otherSentence, sb);
					sb.append("\"");
				}
				sb.append(
						"+\") WHERE ROW$NUM >=\"+ _beginIndex +\" AND ROW$NUM <\"+ _endIndex+\" ORDER BY ROW$NUM\";\r\n");
			}
		}
	}

	protected void forPageQuery() {
		sb.append("int _total = 0;\r\n")

				.append(prefix).append(this.realReturnType).append("> result = new ").append(prefix)
				.append(this.realReturnType).append(">();\r\n").append("result.setPageSize(pageSize);");

		if (this.dynamic) {
			sb.append("StringBuilder _tmpsql = sql;sql= new StringBuilder(); ").append("sql.append(\"SELECT (1) FROM ")
					.append(this.bean.getFromSentence().trim()).append("\");\r\n")

					.append("if(sql.length()>0){ sql.append(_tmpsql);}");
		} else {
			if (this.staticWhereSql == null) {
				sb.append("String sql =\"SELECT (1) FROM ").append(this.bean.getFromSentence().trim())
						.append("\";\r\n");
			} else {
				sb.append("String _tmpsql = sql;");
				sb.append("sql =\"SELECT (1) FROM ").append(this.bean.getFromSentence().trim())
						.append(" WHERE \"+_tmpsql;\r\n");
			}
		}
		sb.append("java.sql.PreparedStatement _pagePs = con.prepareStatement(sql");
		if (this.dynamic) {
			sb.append(".toString()");
		}
		sb.append(");\r\n");
		sb.append("try{");
		this.where.buildParam(sb);
		sb.append(
				"java.sql.ResultSet _pageRs = _pagePs.executeQuery();\r\ntry{if(_pageRs.next()){_total = _pageRs.getInt(1);}}")
				.append("finally{try{_pageRs.close();}catch(Exception e){}}")
				.append("}finally{\r\ntry{_pagePs.close();}catch(Exception e){}\r\n}\r\n");

		sb.append("result.setTotal(_total);")
				.append("if(0== _total){result.setPageNo(1);result.setData(java.util.Collections.<")
				.append(this.realReturnType).append(">emptyList()); return result;}");

		sb.append("if(pageNo==1){");
		this.forPageQueryWithFirstPage();
		sb.append("}else{");
		this.forPageQueryWithNotFirstPage();
		sb.append("}");

	}

	@Override
	protected boolean needRelaceResource() {
		boolean result = this.where.needReplaceResource();

		this.forPageQuery();
		return result;

	}

	@Override
	protected void relaceResource() {
		this.where.replaceResource(sb);

	}

}
