package org.jfw.apt.model.orm;

import java.util.Arrays;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.SqlValue;
import org.jfw.apt.annotation.orm.Where;
import org.jfw.apt.exception.AptException;

public class WhereSentence {

	private ExecutableElement ref;
	private boolean dynamic = false;
	private boolean and = false;
	private String sentence = null;
	private SqlValueEntry[] values;
	private boolean hasNullValue = false;
	private boolean hasFrefix = false;

	public boolean isDynamicWhereSql() {
		if (this.dynamic) {
			return this.hasNullValue;
		} else {
			return false;
		}

	}

	public String getStaticSQL() throws AptException {
		if (this.isDynamicWhereSql())
			throw new AptException(ref, "code bug with use WhereSentence getStaticSQL");
		StringBuilder sb = new StringBuilder();
		if (this.sentence != null)
			Utils.addSqlToStringBuilder(this.sentence, sb);
		if (this.dynamic) {
			for (int i = 0; i < this.values.length; ++i) {
				if (i == 0) {
					if (this.sentence != null) {
						sb.append(this.and ? " AND " : " OR ");
					}
				} else {
					sb.append(this.and ? " AND " : " OR ");
				}
				sb.append(this.values[i].getValueExpression());
			}
		}
		return sb.toString();
	}

	public void appendToSql(StringBuilder sb) throws AptException {
		if (!this.isDynamicWhereSql())
			throw new AptException(ref, "code bug with use WhereSentence appendToSql");
		if (hasFrefix) {
			sb.append("sql.append(\" WHERE ");
			if (this.sentence != null) {
				Utils.addSqlToStringBuilder(this.sentence, sb);
			}
			for (int i = 0; i < this.values.length; ++i) {
				SqlValueEntry sve = this.values[i];
				if (sve.isNullable()) {
					sb.append("if(");
					sve.getOrm().checkNull(sb);
					sb.append("){\r\n").append("sql.append(\"");
					if (this.and)
						sb.append(" AND ");
					else
						sb.append(sb.append(" OR "));
					Utils.addSqlToStringBuilder(sve.getValueExpression(), sb);
					sb.append("\");\r\n}");
				} else {
					if (i != 0 || this.sentence != null) {
						if (this.and)
							sb.append(" AND ");
						else 
							sb.append(" OR ");
					}
					Utils.addSqlToStringBuilder(sve.getValueExpression(), sb);
					if (this.values[i + 1].isNullable()) {
						sb.append("\");");
					}
				}
			}
		} else {
			sb.append("StringBuilder whereSql = new StringBuilder();\r\n int numOfFilers = 0;\r\n");
			for (int i = 0; i < this.values.length; ++i) {
				SqlValueEntry sve = this.values[i];

				sb.append("if(");
				sve.getOrm().checkNull(sb);
				sb.append("){\r\n");
				if (i != 0) {
					sb.append("if(numOfFilers!= 0){ whereSql.append(\"");
					if (this.and)
						sb.append(" AND ");
					else
						sb.append(" OR ");
					sb.append("\");}\r\n");
				}
				sb.append("++numOfFilers;\r\n");
				sb.append("whereSql.append(\"");
				Utils.addSqlToStringBuilder(sve.getValueExpression(), sb);
				sb.append("\");\r\n}\r\n");
			}
			sb.append("if(numOfFilers >0) sql.append(\" WHERE \").append(whereSql.toString());\r\n");
		}

	}

	public boolean needReplaceResource() {
		for (int i = 0; i < this.values.length; ++i) {
			SqlValueEntry sve = this.values[i];
			if (sve.getOrm().isReplaceResource())
				return true;
		}
		return false;
	}

	public void replaceResource(StringBuilder sb) {
		for (int i = 0; i < this.values.length; ++i) {
			SqlValueEntry sve = this.values[i];
			if (sve.getOrm().isReplaceResource())
				sve.getOrm().replaceResource(sb);
		}

	}

	public void prepare(StringBuilder sb) {
		
		if (this.sentence != null) {
			hasFrefix = true;
		} 
		if(this.values.length>0 && this.isDynamicWhereSql()){
			Arrays.sort(this.values);
		}
		if(!hasFrefix && this.values.length>0 && !this.values[0].isNullable()) hasFrefix = true;

		
		if (this.values.length > 0) {
			for (int i = 0; i < this.values.length; ++i) {
				SqlValueEntry sve = this.values[i];
				sve.getOrm().prepare(sb);
			}
		}
	}

	public void buildParam(StringBuilder sb) {
		if (this.values != null && this.values.length > 0) {
			for (int i = 0; i < this.values.length; ++i) {
				SqlValueEntry sve = this.values[i];
				sve.getOrm().writeValue(sb,this.dynamic);
			}
		}
	}

	private WhereSentence() {
	}

	public static WhereSentence build(ExecutableElement ref, Where where, Map<String, Object> map) throws AptException {

		WhereSentence ws = new WhereSentence();
		ws.ref = ref;
		ws.dynamic = where.dynamic();
		ws.and = where.and();
		ws.sentence = Utils.emptyToNull(where.sentence());

		SqlValue[] svs = where.values();
		if (svs.length == 0 && ws.dynamic) {
			throw new AptException(ref, "dynamic where sql @Where.values must be not empty");
		}
		if (ws.dynamic) {
			for (SqlValue sv : svs) {
				if (null == Utils.emptyToNull(sv.valueExpression()))
					throw new AptException(ref, "dynamic where sql @Where.values[?].valueExpression must be not empty");
			}
		}
		ws.values = new SqlValueEntry[svs.length];

		for (int i = 0; i < svs.length; ++i) {
			ws.values[i] = SqlValueEntry.build(svs[i], true, ws.dynamic, ref, map);
			if (!ws.values[i].isPrimitive() && ws.values[i].isNullable())
				ws.hasNullValue = true;
		}
		return ws;
	}

}
