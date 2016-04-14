package org.jfw.util.init;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.jfw.util.ConstData;

public class ClassEntry implements InitEntry{
	private Class<?> clazz;
	private String methodName;
	private Collection<InitEntry> dependBeanIds;
	
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
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
		Method method = this.clazz.getMethod(this.methodName, ConstData.EMPTY_CLASS_ARRAY);
		try {
			method.invoke(null, ConstData.EMPTY_OBJECT_ARRAY);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}		
	}

}
