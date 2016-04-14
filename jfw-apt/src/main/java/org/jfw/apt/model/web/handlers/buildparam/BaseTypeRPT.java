package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.Utils;

public class BaseTypeRPT extends AbstractRequestParamTransfer {

	public void transferToParam(String classType) {
		String cn = classType;
		if (cn.equals("int")) {
			this.sb.append("Integer.parseInt(param)");
		} else if (cn.equals(Integer.class.getName())) {
			this.sb.append("Integer.valueOf(param)");
		} else if (cn.equals(byte.class.getName())) {
			this.sb.append("Byte.parseByte(param)");
		} else if (cn.equals(Byte.class.getName())) {
			this.sb.append("Byte.valueOf(param)");
		} else if (cn.equals(short.class.getName())) {
			this.sb.append("Short.parseShort(param)");
		} else if (cn.equals(Short.class.getName())) {
			this.sb.append("Short.valueOf(param)");
		} else if (cn.equals(long.class.getName())) {
			this.sb.append("Long.parseLong(param)");
		} else if (cn.equals(Long.class.getName())) {
			this.sb.append("Long.valueOf(param)");
		} else if (cn.equals(double.class.getName())) {
			this.sb.append("Double.parseDouble(param)");
		} else if (cn.equals(Double.class.getName())) {
			this.sb.append("Double.valueOf(param)");
		} else if (cn.equals(float.class.getName())) {
			this.sb.append("Float.parseFloat(param)");
		} else if (cn.equals(Float.class.getName())) {
			this.sb.append("Float.valueOf(param)");
		} else if (cn.equals(boolean.class.getName()) || cn.equals(Boolean.class.getName())) {
			this.sb.append("\"1\".equals(param)|| \"true\".equalsIgnoreCase(param)||\"yes\".equalsIgnoreCase(param)");
		} else if (cn.equals(String.class.getName())) {
			this.sb.append("param");
		} else if (cn.equals(java.math.BigInteger.class.getName())) {
			this.sb.append("new  java.math.BigInteger(param)");
		} else if (cn.equals(java.math.BigDecimal.class.getName())) {
			this.sb.append("new java.math.BigDecimal(param)");
		} else {
			throw new IllegalArgumentException("not supported class type:" + cn);
		}

	}

	@Override
	public void bulidParam() {
		String paramName = this.annotation.getParamNameInRequest();
		this.rmcg.readParameter(this.sb, paramName);
		if (this.annotation.isRequired()) {
			sb.append("if(null==param || param.length()==0){");
			this.raiseNoFoundError(paramName);
			sb.append("}\r\n");
			sb.append(this.mpe.getTypeName()).append(" ").append(this.mpe.getName()).append(" = ");
			this.transferToParam(this.mpe.getTypeName());
			sb.append(";\r\n");
		} else {
			sb.append(this.mpe.getTypeName()).append(" ").append(this.mpe.getName());
			String dv = this.annotation.getDefaultValue();
			if (Utils.isPrimitive(this.mpe.getTypeName())) {
				if (dv != null && dv.trim().length() != 0 && !"null".equals(dv.trim())) {
					sb.append(" = ").append(dv);
				}
			} else {
				sb.append(" = ").append((dv == null || dv.trim().length() == 0) ? "null" : dv);
			}
			sb.append(";\r\n");
			sb.append("if(null!=param && param.length()!=0){\r\n");
			sb.append(this.mpe.getName()).append(" = ");
			this.transferToParam(this.mpe.getTypeName());
			sb.append(";\r\n");
			sb.append("}\r\n");
		}
	}

	@Override
	public void bulidBeanProterty() {
		this.checkRequestFieldParamName();
		this.rmcg.readParameter(sb, this.frp.getParamName().trim());
		if (this.frp.isRequired()) {
			sb.append("if(null==param || param.length()==0){");
			this.raiseNoFoundError(this.frp.getParamName().trim());
			sb.append("}\r\n");
			Utils.writeSetterBeforePart(sb, this.mpe.getName(), this.frp.getValue());
			this.transferToParam(this.frp.getValueClassName());
			sb.append(");\r\n");
		} else {
			sb.append("if(null!=param && param.length()!=0){\r\n");
			Utils.writeSetterBeforePart(sb, this.mpe.getName(), this.frp.getValue());
			this.transferToParam(this.frp.getValueClassName());
			sb.append(");\r\n");
			sb.append("}");
			String dv = this.frp.getDefaultValue();
			if(dv!=null&& dv.trim().length()>0){
				sb.append("else{\r\n");
				Utils.writeSetter(sb,this.mpe.getName(),this.frp.getValue(), dv);
				sb.append("}");
			}
			sb.append("\r\n");
		}
	}

}
