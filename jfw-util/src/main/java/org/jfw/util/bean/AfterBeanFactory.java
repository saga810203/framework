package org.jfw.util.bean;

public interface AfterBeanFactory {
	void handle(BeanFactory bf) throws Throwable;
}
