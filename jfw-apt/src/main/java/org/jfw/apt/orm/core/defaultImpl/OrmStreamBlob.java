package org.jfw.apt.orm.core.defaultImpl;

import java.io.InputStream;
import java.util.Map;

public class OrmStreamBlob extends BaseOrmHandler {
	protected String valueEl;
	protected boolean userTempalteVar;
	protected boolean valueNullable;
	protected Map<String, Object> localVarInMethod;
	protected String isNullVariable;
	protected String cacheValueVariable;

	protected String blobName;

	@Override
	public Class<?> supportsClass() {
		return InputStream.class;
	}

	@Override
	public void readValue(StringBuilder sb, String beforCode, String afterCode, int colIndex, boolean dbNullable,
			Map<String, Object> localVarInMethod) {
		throw new RuntimeException("jfw orm handler OrmStreamBlob only support write method");
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
		this.blobName = this.getTempalteVariableName(localVarInMethod);
		sb.append("java.sql.Blob ").append(this.blobName).append(" = null;");

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
			sb.append(this.blobName).append(" =  con.createBlob();\r\n");
			sb.append("org.jfw.util.io.IoUtil.copy(").append(this.getValueVariable()).append(",").append(this.blobName)
					.append(".setBinaryStream(1),true,true);");
			sb.append("ps.setBlob(_index++,").append(this.blobName).append(");\r\n");
		} else {

			if (dynamicValue) {
				sb.append("if(").append(this.isNullVariable).append("){\r\n");
				sb.append(this.blobName).append(" =  con.createBlob();\r\n");
				sb.append("org.jfw.util.io.IoUtil.copy(").append(this.getValueVariable()).append(",")
						.append(this.blobName).append(".setBinaryStream(1),true,true);");
				sb.append("ps.setBlob(_index++,").append(this.blobName);
				sb.append(");\r\n}\r\n");
			} else {
				sb.append("if(").append(this.isNullVariable).append("){\r\n")
						.append("ps.setNull(_index++,java.sql.Types.BLOB);\r\n").append("}else{\r\n");
				sb.append(this.blobName).append(" =  con.createBlob();\r\n");
				sb.append("org.jfw.util.io.IoUtil.copy(").append(this.getValueVariable()).append(",")
						.append(this.blobName).append(".setBinaryStream(1),true,true);");
				sb.append("ps.setBlob(_index++,").append(this.blobName);
				sb.append(");\r\n}\r\n");
			}
		}

	}

	private String getValueVariable() {
		return this.cacheValueVariable == null ? this.valueEl : this.cacheValueVariable;
	}

	@Override
	public boolean isReplaceResource() {
		return true;
	}

	@Override
	public void replaceResource(StringBuilder sb) {
		sb.append("try{if(null!=").append(this.blobName).append(")").append(this.blobName).append(".free();}catch(Throwable th){}");
	}

}
