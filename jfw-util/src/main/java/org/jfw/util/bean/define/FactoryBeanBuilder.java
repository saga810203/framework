package org.jfw.util.bean.define;

import java.lang.reflect.Method;

import org.jfw.util.bean.BeanBuilder;
import org.jfw.util.bean.BeanFactory;

public class FactoryBeanBuilder extends BeanDefine implements BeanBuilder {
	private final static Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	private String beanid;
	private String method;

	protected FactoryBeanBuilder(BeanFactory bf, String key, String value) {
		super(bf, key, value);
	}

	public static BeanDefine build(String key, String value, BeanFactory bf) throws ConfigException {

		FactoryBeanBuilder result = new FactoryBeanBuilder(bf, key, value);
		result.beanid = value;
		if (!bf.contains(value))
			throw new ConfigException("invalid beanid[" + value + "]");

		return result;
	}

	@Override
	public void addAttributeInternal(BeanFactory bf, String attrName, String attrVal) throws ConfigException {
		if (attrName.equals("factory-method")) {
			this.method = attrVal;
		}
		this.addDependBean(this.beanid);
	}

	@Override
	public BeanBuilder buildBeanBuilder(BeanFactory bf) {
		return this;
	}


	@Override
	public Object build(BeanFactory bf) {
		Object obj = bf.getBean(this.beanid);
		try {
			Method m = obj.getClass().getMethod(this.method, EMPTY_CLASS_ARRAY);
			//TODO: check static abstract returnType
			return m.invoke(obj,EMPTY_OBJECT_ARRAY);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void config(Object obj, BeanFactory bf) {
	}

}
