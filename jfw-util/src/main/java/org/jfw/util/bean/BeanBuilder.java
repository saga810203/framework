package org.jfw.util.bean;

public interface BeanBuilder {
	Object build(BeanFactory bf);
	void config(Object obj,BeanFactory bf);
}
