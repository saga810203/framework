package org.jfw.apt.out.model;

public class BuilderBeanDefine extends BeanDefine{
	public BuilderBeanDefine(String classname,String staticMethodName){
		this.addFalg("build");
		this.value = classname;
		BeanAttributeDefine bad = new BeanAttributeDefine();
		bad.setKey("build-method");
		bad.setValue(staticMethodName);
		this.attributes.add(bad);
	}

}
