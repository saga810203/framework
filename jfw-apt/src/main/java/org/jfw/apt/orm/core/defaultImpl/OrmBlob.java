package org.jfw.apt.orm.core.defaultImpl;

import java.util.Map;

public class OrmBlob extends BaseOrmHandler {
	protected String valueEl;
	protected boolean userTempalteVar;
	protected boolean valueNullable;
	protected Map<String, Object> localVarInMethod;
	protected String isNullVariable;
	protected String cacheValueVariable;
	
	@Override
	public Class<?> supportsClass() {
		return byte[].class;
	}

	@Override
	public void readValue(StringBuilder sb, String beforCode, String afterCode, int colIndex, boolean dbNullable,
			Map<String, Object> localVarInMethod) {
		String bytes = this.getTempalteVariableName(localVarInMethod);
		sb.append("byte[] ").append(bytes).append(" =  null;\r\n");
		String localVar = this.getTempalteVariableName(localVarInMethod);

		sb.append(" java.sql.Blob ").append(localVar).append(" = rs.getBlob(").append(colIndex).append(");\r\n");
		sb.append("try{\r\n");
		if (dbNullable) {
			sb.append("if(!rs.wasNull()){\r\n ");
		}
		sb.append(bytes).append("=org.jfw.util.io.IoUtil.readStream(").append(localVar).append(".getBinaryStream(),true);\r\n");

		if (dbNullable) {
			sb.append("}\r\n");
		}
		sb.append("}finally{\r\n   if(null!=").append(localVar).append("){").append(localVar).append(".free();}\r\n")
				.append("}\r\n");

		sb.append(beforCode == null ? "" : beforCode).append(bytes).append(afterCode == null ? "" : afterCode);
	}

	

	@Override
	public void init(String valueEl, boolean userTempalteVar, boolean valueNullable,
			Map<String, Object> localVarInMethod) {
		this.valueEl = valueEl;
		this.userTempalteVar = userTempalteVar;
		this.valueNullable = valueNullable;
		this.localVarInMethod = localVarInMethod;
	}

	@Override
	public void prepare(StringBuilder sb) {
		this.checkParamIndex(sb, this.localVarInMethod);
		this.cacheValueVariable = null;
		this.isNullVariable = null;
		if (this.userTempalteVar) {
			this.cacheValueVariable = this.getTempalteVariableName(localVarInMethod);
			sb.append(this.supportsClass().getName()).append(" ").append(this.cacheValueVariable).append(" = ")
					.append(this.valueEl).append(";\r\n");
		}
		if (!this.valueNullable)
			return;

		this.isNullVariable = this.getTempalteVariableName(this.localVarInMethod);

		sb.append("boolean ").append(this.isNullVariable).append(" = ");
		if (null == this.cacheValueVariable) {
			sb.append(" null == ").append(this.valueEl).append(";\r\n");
		} else {
			sb.append("null == ").append(this.cacheValueVariable).append(";\r\n");
		}

	}

	@Override
	public void checkNull(StringBuilder sb) {
		if (null == this.isNullVariable)
			throw new UnsupportedOperationException();
		sb.append(this.isNullVariable);

	}

	@Override
	public void writeValue(StringBuilder sb, boolean dynamicValue) {
		this.checkParamIndex(sb, localVarInMethod);

		if (null == this.isNullVariable) {
			sb.append("ps.setBytes(_index++,");
			if (null == cacheValueVariable) {
				sb.append(this.valueEl);
			} else {
				sb.append(this.cacheValueVariable);
			}
			sb.append(");\r\n");
		} else {
			if (dynamicValue) {
				sb.append("if(").append(this.isNullVariable).append("){\r\n").append("ps.setBytes(_index++,");
				if (null == cacheValueVariable) {
					sb.append(this.valueEl);
				} else {
					sb.append(this.cacheValueVariable);
				}
				sb.append(");\r\n}\r\n");
			} else {
				sb.append("if(").append(this.isNullVariable).append("){\r\n")
				.append("ps.setNull(_index++,java.sql.Types.BLOB);\r\n").append("}else{\r\n").append("ps.setBytes(_index++,");
				if (null == cacheValueVariable) {
					sb.append(this.valueEl);
				} else {
					sb.append(this.cacheValueVariable);
				}
				sb.append(");\r\n}\r\n");
			}
		}


	}

	@Override
	public boolean isReplaceResource() {
		return false;
	}

	@Override
	public void replaceResource(StringBuilder sb) {
	}

}
