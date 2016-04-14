package org.jfw.util.execut;

import java.lang.reflect.Method;

public class SchedulerBean {
	private Object bean;
	private String jobMethodName;
	private String interruptMethodName;
	
	private Method jobMethod;
	private Method interruptMethod;

	private String cron;
	private long fixedDelay = -1;
	private long fixedRate = -1;
	
	
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public long getFixedDelay() {
		return fixedDelay;
	}
	public void setFixedDelay(long fixedDelay) {
		this.fixedDelay = fixedDelay;
	}
	public long getFixedRate() {
		return fixedRate;
	}
	public void setFixedRate(long fixedRate) {
		this.fixedRate = fixedRate;
	}


}
