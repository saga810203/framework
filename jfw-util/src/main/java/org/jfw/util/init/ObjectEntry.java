package org.jfw.util.init;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.jfw.util.ConstData;
import org.jfw.util.bean.BeanFactory;

public class ObjectEntry implements InitEntry{
	private String beanId;
	private String methodName;
	private Collection<InitEntry> dependBeanIds;
	private BeanFactory bf ;

	public BeanFactory getBeanFactory() {
		return bf;
	}
	public void setBeanFactory(BeanFactory bf) {
		this.bf = bf;
	}
	public String getBeanId() {
		return beanId;
	}
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Collection<InitEntry> getDependBeanIds() {
		return dependBeanIds;
	}
	public void setDependBeanIds(Collection<InitEntry> dependBeanIds) {
		this.dependBeanIds = dependBeanIds;
	}
	
	public void execute() throws Throwable {
		Object obj = this.bf.getBean(this.beanId);
		Method method = obj.getClass().getMethod(this.methodName, ConstData.EMPTY_CLASS_ARRAY);
		try {
			method.invoke(obj, ConstData.EMPTY_OBJECT_ARRAY);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}		
	}

}
