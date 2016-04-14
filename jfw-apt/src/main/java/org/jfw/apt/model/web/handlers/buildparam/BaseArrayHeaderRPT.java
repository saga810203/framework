package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.Utils;

public class BaseArrayHeaderRPT extends AbstractRequestHeaderTransfer {

	public void transferToParams(String classType) {
		String cn = classType;

		if (cn.equals("int[]")) {
			this.sb.append("Integer.parseInt(params[i])");
		} else if (cn.equals("java.lang.Integer[]")) {
			this.sb.append("Integer.valueOf(params[i])");
		} else if (cn.equals("byte[]")) {
			this.sb.append("Byte.parseByte(params[i])");
		} else if (cn.equals("java.lang.Byte[]")) {
			this.sb.append("Byte.valueOf(params[i])");
		} else if (cn.equals("short[]")) {
			this.sb.append("Short.parseShort(params[i])");
		} else if (cn.equals("java.lang.Short[]")) {
			this.sb.append("Short.valueOf(params[i])");
		} else if (cn.equals("long[]")) {
			this.sb.append("Long.parseLong(params[i])");
		} else if (cn.equals("java.lang.Long[]")) {
			this.sb.append("Long.valueOf(params[i])");
		} else if (cn.equals("double[]")) {
			this.sb.append("Double.parseDouble(params[i])");
		} else if (cn.equals("java.lang.Double[]")) {
			this.sb.append("Double.valueOf(params[i])");
		} else if (cn.equals("float[]")) {
			this.sb.append("Float.parseFloat(params[i])");
		} else if (cn.equals("java.lang.Float[]")) {
			this.sb.append("Float.valueOf(params[i])");
		} else if (cn.equals("boolean[]") || cn.equals("java.lang.Boolean[]")) {
			this.sb.append(
					"\"1\".equals(params[i])|| \"true\".equalsIgnoreCase(params[i])||\"yes\".equalsIgnoreCase(params[i])");
		} else if (cn.equals("java.lang.String[]")) {
			this.sb.append("params[i]");
		} else if (cn.equals("java.math.BigInteger[]")) {
			this.sb.append("new java.math.BigInteger(params[i])");
		} else if (cn.equals("java.math.BigDecmal[]")) {
			this.sb.append("new java.math.BigDecimal(params[i])");
		} else {
			throw new IllegalArgumentException("not supperted class type:" + cn);
		}
	}

	@Override
	public void bulidParam() {
		String vTypeName = this.mpe.getTypeName();

		String paramName =this.annotation.getParamNameInRequest();

		this.rmcg.readHeaders(this.sb, paramName);

		if (this.annotation.isRequired()) {
			sb.append("if(null==params || params.length==0){");
			this.raiseNoFoundError(paramName);
			sb.append("}\r\n").append(vTypeName).append(" ").append(this.mpe.getName()).append(" = new ")
					.append(vTypeName.substring(0, vTypeName.length() - 1)).append("params.length];\r\n");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(this.mpe.getName()).append("[i]=");
			this.transferToParams(this.mpe.getTypeName());
			sb.append(";\r\n}\r\n");
		} else {
			sb.append(vTypeName).append(" ").append(this.mpe.getName()).append(";\r\n");

			sb.append("if(null!=params && params.length!=0){");
			sb.append(this.mpe.getName()).append(" = new ").append(vTypeName.substring(0, vTypeName.length() - 1))
					.append("params.length];");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(this.mpe.getName()).append("[i]=");
			this.transferToParams(this.mpe.getTypeName());
			sb.append(";\r\n}\r\n");
			sb.append("}else{\r\n");
			String dv = this.annotation.getDefaultValue();
			sb.append(this.mpe.getName()).append(" = ")
					.append((dv == null || dv.trim().length() == 0) ? "null" : dv)
					.append(";\r\n");
			sb.append(";\r\n}\r\n");
		}
	}

	@Override
	public void bulidBeanProterty() {
		String vTypeName = this.mpe.getTypeName();
		String localName = this.rmcg.getTempalteVariableName();
		this.checkRequestFieldParamName();
		this.rmcg.readHeaders(sb, this.frp.getParamName().trim());
		if (this.frp.isRequired()) {
			sb.append("if(null==params || params.length==0){");
			this.raiseNoFoundError(this.frp.getParamName().trim());
			sb.append("}\r\n");
			sb.append(vTypeName).append(" ").append(localName).append(" = new ")
					.append(vTypeName.substring(1, vTypeName.length() - 1)).append("params.length];\r\n");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(localName).append("[i]=");
			this.transferToParams(this.frp.getValueClassName());
			sb.append(";\r\n}\r\n");
			Utils.writeSetter(sb,this.mpe.getName(), this.frp.getValue(), localName);
		} else {
			sb.append("if(null!=params && params.length!=0){\r\n");
			sb.append(vTypeName).append(" ").append(localName).append(" = new ")
					.append(vTypeName.substring(1, vTypeName.length() - 1)).append("params.length];\r\n");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(localName).append("[i]=");
			this.transferToParams(this.frp.getValueClassName());
			sb.append(";\r\n}\r\n");
			Utils.writeSetter(sb,this.mpe.getName(), this.frp.getValue(), localName);
			sb.append("\r\n}\r\n");
			String dv = this.frp.getDefaultValue();
			if (dv != null && dv.trim().length() != 0) {
				sb.append("else{");
				Utils.writeSetter(sb,this.mpe.getName(), this.frp.getValue(), dv);
				sb.append("\r\n}\r\n");
			}
		}
	}
}
