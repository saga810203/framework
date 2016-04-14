package org.jfw.util.bean.define;

import org.jfw.util.bean.BeanFactory;

public class RefPrototypeBeanVD extends ValueDefine {
	@Override
	public Object getValue(BeanFactory bf) {
		return bf.getBean(this.beanid);
	}

	private String beanid;

	@Override
	public void init(BeanFactory bf, String name, Class<?> clazz, boolean isRef, String val) {
		this.name = name;
		this.clazz = clazz;
		this.beanid = val;
	}
}
