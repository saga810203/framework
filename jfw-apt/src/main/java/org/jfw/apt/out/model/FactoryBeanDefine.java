package org.jfw.apt.out.model;

public class FactoryBeanDefine extends BeanDefine{
	public FactoryBeanDefine(String beanid,String methodName){
		this.addFalg("factory");
		this.value=beanid;
		BeanAttributeDefine bad = new BeanAttributeDefine();
		bad.setKey("factory-method");
		bad.setValue(methodName);
		this.addAttribute(bad);
	}
}
