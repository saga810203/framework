package org.jfw.apt.out.model;

public class ClassBeanDefine extends BeanDefine{
	public static String classnameToBeanId(String classname){
		return classname.replaceAll("\\.","_");
	}	
	
	public ClassBeanDefine(String className){
		this.value = className;
		this.id = classnameToBeanId(className);
	}
	
	public void setAttribute(String key,String val,String className){
		BeanAttributeDefine attr = new BeanAttributeDefine();
		attr.setKey(key);
		attr.setRef(false);
		attr.setValue(val);
		attr.setValueClassName(className);	
		this.attributes.add(attr);
	}
	public void setString(String key,String val){
		this.setAttribute(key, val, null);
	}
	public void setInt(String key,String val){
		this.setAttribute(key, val, "int");
	}
	public void setByte(String key,String val){
		this.setAttribute(key, val, "byte");
	}
	public void setLong(String key,String val){
		this.setAttribute(key, val, "long");
	}
	public void setBoolean(String key,String val){
		this.setAttribute(key, val,"boolean");
	}
	public void setFloat(String key,String val){
		this.setAttribute(key, val, "float");
	}
	public void setDouble(String key,String val){
		this.setAttribute(key, val, "double");
	}
	
	public void setClass(String key,String val){
		this.setAttribute(key, val, "java.lang.Class");
	}
	public void setRefAttribute(String key,String val){
		BeanAttributeDefine attr = new BeanAttributeDefine();
		attr.setKey(key);
		attr.setRef(false);
		attr.setValue(val);
		attr.setValueClassName(null);	
		this.addAttribute(attr);
	}

}
