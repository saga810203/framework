package org.jfw.util.execut.task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jfw.util.ConstData;

public class BeanCronTask extends CronTask {
	private Object bean;
	private Method runMethod;
	private Method interruptMethod;

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public void setRunMethod(Method runMethod) {
		this.runMethod = runMethod;
	}

	public void setInterruptMethod(Method interruptMethod) {
		this.interruptMethod = interruptMethod;
	}

	@Override
	protected void runInternal() throws Throwable {
		this.runMethod.invoke(this.bean, ConstData.EMPTY_OBJECT_ARRAY);

	}

	@Override
	protected void interruptInternal() {
		if (this.interruptMethod != null) {
			try {
				this.interruptMethod.invoke(this.bean, ConstData.EMPTY_OBJECT_ARRAY);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}

	}

}
