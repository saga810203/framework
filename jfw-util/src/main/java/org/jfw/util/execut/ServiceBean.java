package org.jfw.util.execut;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.jfw.util.ConstData;

public class ServiceBean implements Service {
	private Object bean;
	private String startupMethodName;
	private String shutdownMethodName;
	
	
	private Method startupMethod;
	private Method shutdownMethod;
	private List<String> dependBeanNames;
	
	
	public List<String> getDependBeanNames() {
		return dependBeanNames;
	}
	public void setDepends(List<String> dependBeanNames) {
		this.dependBeanNames = dependBeanNames;
	}
	public void setBean(Object bean) {
		this.bean = bean;
	}
	public void setStartupMethodName(String startupMethodName) {
		this.startupMethodName = startupMethodName;
	}
	public void setShutdownMethodName(String shutdownMethodName) {
		this.shutdownMethodName = shutdownMethodName;
	}
	
	private void init() throws NoSuchMethodException, SecurityException{
		Class<?> clazz = this.bean.getClass();
		this.startupMethod = clazz.getMethod(this.startupMethodName,ConstData.EMPTY_CLASS_ARRAY);
		this.shutdownMethod= clazz.getMethod(this.shutdownMethodName, ConstData.EMPTY_CLASS_ARRAY);
	}
	@Override
	public void startup() throws Exception{
		this.init();
		this.startupMethod.invoke(this.bean, ConstData.EMPTY_OBJECT_ARRAY);
	}
	@Override
	public void shutdown() {
		try {
			this.shutdownMethod.invoke(this.bean, ConstData.EMPTY_OBJECT_ARRAY);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			
		}
	}
	
	

}
