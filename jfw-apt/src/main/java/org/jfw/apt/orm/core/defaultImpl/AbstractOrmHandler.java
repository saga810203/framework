package org.jfw.apt.orm.core.defaultImpl;

import java.util.Map;

public abstract class AbstractOrmHandler extends BaseOrmHandler {

	protected String valueEl;
	protected boolean userTempalteVar;
	protected boolean valueNullable;
	protected Map<String, Object> localVarInMethod;
	protected String isNullVariable;
	protected String cacheValueVariable;

	public abstract String getReadMethod();

	public abstract String getWriteMethod();

	@Override
	public void readValue(StringBuilder sb, String beforCode, String afterCode, int colIndex, boolean dbNullable,
			Map<String, Object> localVarInMethod) {
		if ((!dbNullable) || this.supportsClass().isPrimitive()) {
			sb.append(beforCode == null ? "" : beforCode).append("rs.").append(this.getReadMethod()).append("(")
					.append(colIndex).append(")").append(afterCode == null ? "" : afterCode);
		} else {
			String localVar = this.getTempalteVariableName(localVarInMethod);
			sb.append(this.supportsClass().getName()).append(" ").append(localVar).append(" = rs.")
					.append(this.getReadMethod()).append("(").append(colIndex).append(");\r\n")
					.append("if(rs.wasNull()){\r\n").append(localVar).append(" = null;\r\n}\r\n")
					.append(beforCode == null ? "" : beforCode).append(localVar)
					.append(afterCode == null ? "" : afterCode);
		}
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
		if (this.supportsClass().isPrimitive())
			return;
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
			sb.append("ps.").append(this.getWriteMethod()).append("(paramIndex++,");
			if (null == cacheValueVariable) {
				sb.append(this.valueEl);
			} else {
				sb.append(this.cacheValueVariable);
			}
			sb.append(");\r\n");
		} else {
			if (dynamicValue) {
				sb.append("if(").append(this.isNullVariable).append("){\r\n").append("ps.")
						.append(this.getWriteMethod()).append("(paramIndex++,");
				if (null == cacheValueVariable) {
					sb.append(this.valueEl);
				} else {
					sb.append(this.cacheValueVariable);
				}
				sb.append(");\r\n}\r\n");
			} else {
				sb.append("if(").append(this.isNullVariable).append("){\r\n").append("ps.setNull(paramIndex++,")
						.append(this.getSqlType()).append(");\r\n").append("}else{\r\n").append("ps.")
						.append(this.getWriteMethod()).append("(paramIndex++,");
				if (null == cacheValueVariable) {
					sb.append(this.valueEl);
				} else {
					sb.append(this.cacheValueVariable);
				}
				sb.append(");\r\n}\r\n");
			}
		}

	}

	protected abstract int getSqlType();

	@Override
	public boolean isReplaceResource() {
		return false;
	}

	@Override
	public void replaceResource(StringBuilder sb) {
	}
}
