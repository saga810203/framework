package org.jfw.util.bean.define;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jfw.util.bean.BeanBuilder;
import org.jfw.util.bean.BeanFactory;

public class StaticBuildBeanDefine extends BeanDefine {
	private final static Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0]; 
	private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0]; 
	
	private Class<?> clazz;
	private Method method;

	protected StaticBuildBeanDefine(BeanFactory bf, String key, String value) {
		super(bf, key, value);
	}
	
	
	public static BeanDefine build(String key, String value, BeanFactory bf) throws ConfigException {

		StaticBuildBeanDefine result = new StaticBuildBeanDefine(bf, key, value);
		try {
			result.clazz = Class.forName(value);
		} catch (ClassNotFoundException e) {
			throw new ConfigException("invalid classname[" + value + "]", e);
		}
		return result;
	}

	@Override
	public void addAttributeInternal(BeanFactory bf, String attrName, String attrVal) throws ConfigException {
		if(attrName.equals("build-method")){
			try {
				this.method = this.clazz.getMethod(attrVal, EMPTY_CLASS_ARRAY);
			} catch (Exception e) {
				throw new ConfigException("unfound method["+attrVal+"()] class[" + this.clazz.getName() + "]");
			}
			if((!Modifier.isStatic(this.method.getModifiers()))|| Modifier.isAbstract(this.method.getModifiers())|| void.class.equals(this.method.getReturnType()))
				throw new ConfigException("invalid method["+attrVal+"()] class[" + this.clazz.getName() + "]");
			
		}
		
	}

	@Override
	public BeanBuilder buildBeanBuilder(BeanFactory bf) {
		Builder builder = new Builder();
		builder.className = this.clazz.getName();
		builder.method = this.method;
		return builder;
	}
	private static class Builder implements BeanBuilder{
		private String className;
		private Method method;
		@Override
		public Object build(BeanFactory bf) {
			try {
				return method.invoke(null,EMPTY_OBJECT_ARRAY);
			} catch (Exception e) {
				throw new RuntimeException("invoke method[" + className + "."+method.getName()+"()]", e);
			}
		}
		
	}

}
