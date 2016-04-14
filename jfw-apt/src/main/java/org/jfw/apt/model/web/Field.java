package org.jfw.apt.model.web;

import org.jfw.apt.annotation.web.FieldParam;

public class Field{
	public String getName() {
		return name;
	}
	public String getClassName() {
		return className;
	}
	public String getParamName() {
		return paramName;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public boolean isRequired() {
		return required;
	}
	private String name;
	private String className;
	private String paramName;
	private String defaultValue ="";
	private boolean required = false;
	
	
	public static Field build(FieldParam fp){
		Field result = new Field();
		result.name = fp.value();
		result.className = fp.valueClassName();
		result.paramName = fp.paramName();
		result.defaultValue = fp.defaultValue();
		result.required = fp.required();			
		return result;
	}

}