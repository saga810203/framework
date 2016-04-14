package org.jfw.util.bean.define;

import org.jfw.util.bean.BeanFactory;

public class RefSingleBeanVD extends ValueDefine {

	@Override
	public Object getValue(BeanFactory bf) {
		if (null == this.cacheObject) {
			this.cacheObject = bf.getBean(this.beanid);
		}
		return this.cacheObject;
	}

	private String beanid;
	private Object cacheObject = null;

	@Override
	public void init(BeanFactory bf, String name, Class<?> clazz, boolean isRef, String val) {
		this.name = name;
		this.clazz = clazz;
		this.beanid = val;
	}

}
