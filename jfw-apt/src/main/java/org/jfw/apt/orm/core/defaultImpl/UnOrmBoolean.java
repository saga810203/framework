package org.jfw.apt.orm.core.defaultImpl;

import java.util.Map;

public class UnOrmBoolean extends BaseOrmHandler {
	private Map<String, Object> lmap;
	private String valueEl;

	@Override
	public Class<?> supportsClass() {
		return boolean.class;
	}

	@Override
	public void readValue(StringBuilder sb, String beforCode, String afterCode, int colIndex, boolean dbNullable,
			Map<String, Object> localVarInMethod) {
		sb.append(beforCode == null ? "" : beforCode).append("!\"0\".equals(rs.getString(").append(colIndex)
				.append("))").append(afterCode == null ? "" : afterCode);

	}

	@Override
	public void init(String valueEl, boolean userTempalteVar, boolean valueNullable,
			Map<String, Object> localVarInMethod) {
		lmap = localVarInMethod;
		this.valueEl = valueEl;

	}

	@Override
	public void prepare(StringBuilder sb) {
		this.checkParamIndex(sb, this.lmap);

	}

	@Override
	public void checkNull(StringBuilder sb) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void writeValue(StringBuilder sb,boolean dynamicValue) {
		this.checkParamIndex(sb, lmap);
		sb.append("if(").append(this.valueEl).append("){\r\n").append("ps.setString(paramIndex++,\"1\");\r\n}else{\r\n")
				.append("ps.setString(paramIndex++,\"0\");\r\n}\r\n");
	}

	@Override
	public boolean isReplaceResource() {
		return false;
	}

	@Override
	public void replaceResource(StringBuilder sb) {
	}

}
