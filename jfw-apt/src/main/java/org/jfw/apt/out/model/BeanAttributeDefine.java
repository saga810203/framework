package org.jfw.apt.out.model;

public class BeanAttributeDefine {
	private String value;
	private String key;
	private String valueClassName;
	private boolean ref = false;
	public void setValue(String value) {
		this.value = value;
	}
	public void setKey(String key) {
		this.key = key.trim().replaceAll("\\.","_");
	}
	public void setValueClassName(String valueClassName) {
		this.valueClassName = valueClassName;
	}
	public void setRef(boolean ref) {
		this.ref = ref;
	}


	public final void appendToStringBuilder(StringBuilder sb,BeanDefine bd){
		sb.append("\r\n").append(bd.getId()).append(".").append(this.key);
		if(this.ref){
			sb.append("-ref");
		}else{
			if(valueClassName!=null&& this.valueClassName.length()>0)
				sb.append("::").append(this.valueClassName);
		}
		sb.append("=").append(this.value);
	}
}
