package org.jfw.util.bean.define;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jfw.util.bean.BeanBuilder;
import org.jfw.util.bean.BeanFactory;

public class ClassBeanDefine extends BeanDefine {
	private Class<?> clazz;

	private Map<Method, ValueDefine> values = new HashMap<Method, ValueDefine>();


	public static BeanDefine build(String key, String val, BeanFactory bf) throws ConfigException {
		ClassBeanDefine cbf = new ClassBeanDefine(bf, key, val);
		try {
			cbf.clazz = Class.forName(val.trim());
		} catch (Exception e) {
			throw new ConfigException("invalid classname[" + val + "]", e);
		}
		return cbf;
	}

	protected ClassBeanDefine(BeanFactory bf, String key, String value) {
		super(bf, key, value);
	}

	private String buildSetterName(String attrName) {
		if (attrName.length() == 1) {
			return "set" + attrName.toUpperCase(Locale.US);
		}
		return "set" + attrName.substring(0, 1).toUpperCase(Locale.US) + attrName.substring(1);
	}

	private Method findMethod(String methodName, ValueDefine vd) throws ConfigException {
		Method method = null;
		Class<?> baseClass =  null;
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(methodName) && m.getParameterTypes().length == 1) {
				if (vd.getValueClass() == null) {

					if (method != null) {
						if (method.isBridge() && (!m.isBridge())) {

						} else if ((!method.isBridge()) && m.isBridge()) {
							method = m;
						}else{
							throw new ConfigException("exists mulit method["+methodName+"]");
						}
					}else{
						method = m;
					}
				} else {
					Class<?> mClass = m.getParameterTypes()[0];
					if (mClass.equals(vd.getValueClass())) {
						method = m;
						break;
					}else if(mClass.isAssignableFrom(vd.getValueClass())){
						if(method == null){
							method = m;
							baseClass= mClass;
						}else if(baseClass.isAssignableFrom(mClass)){
							method = m;
							baseClass= mClass;
						}	
					}
				}
			}
		}
		return method;
	}

	@Override
	public void addAttributeInternal(BeanFactory bf, String name, String attrVal) throws ConfigException {
		String className = null;
		String attrName = name;
		int index = attrName.indexOf("::");
		if (index > 0) {
			className = attrName.substring(index + 2);
			attrName =  attrName.substring(0, index);
		}
		boolean isRef = attrName.endsWith("-ref");
		if (isRef) {
			attrName = attrName.substring(0, attrName.length() - 4);
		}
		String methodName = this.buildSetterName(attrName);
		for (Method m : this.values.keySet()) {
			if (m.getName().equals(methodName))
				throw new ConfigException("duplicate bean attribute[" + attrName + "]");
		}

		Class<?> cls = null;
		if (className != null && className.trim().length() > 0) {
			cls = ValueDefine.converToValueClass(className.trim());
		}
		ValueDefine vd = ValueDefine.build(bf, attrName, cls, isRef, attrVal);
		
		Method method = this.findMethod(methodName, vd);
		if(method==null) throw new ConfigException("not found attribute[" + attrName + "] in class["+this.clazz.getName()+"]");

		if (isRef){
			if(!this.addDependBean(attrVal.trim())){
				 throw new ConfigException("not found ref bean[" + attrVal + "]");
			}
		}
		this.values.put(method,vd);
	}

	@Override
	public BeanBuilder buildBeanBuilder(BeanFactory bf) {
		Builder b = new Builder();
		b.clazz = clazz;
		b.attrs = values;
		return b;
	}

	private static class Builder implements BeanBuilder {
		private Class<?> clazz;

		private Map<Method,ValueDefine> attrs;

		@Override
		public Object build(BeanFactory bf) {
			Object result;
			try {
				result = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("create class[" + clazz.getName() + "] instance error:", e);
			}
			return result;
		}

		@Override
		public void config(Object obj, BeanFactory bf) {
			
			if(!attrs.isEmpty())
			for(Map.Entry<Method, ValueDefine> entry:attrs.entrySet()){
				try {
					entry.getKey().invoke(obj,entry.getValue().getValue(bf));
				} catch (Exception e) {
					throw new RuntimeException("set attibute[" + entry.getValue().getName() + "] error:", e);
				}
			}
		}
	}

}
