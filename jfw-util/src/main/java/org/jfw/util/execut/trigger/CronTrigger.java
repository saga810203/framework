package org.jfw.util.execut.trigger;

import java.util.Date;
import java.util.TimeZone;

import org.jfw.util.execut.Trigger;

public class CronTrigger implements Trigger{
	private CronContext cron;
	
	private boolean skipOverTime = false;
	
	public boolean isSkipOverTime() {
		return skipOverTime;
	}

	public void setSkipOverTime(boolean skipOverTime) {
		this.skipOverTime = skipOverTime;
	}

	public CronTrigger(String cronExpression){
		this.cron = new CronContext(cronExpression,TimeZone.getDefault());
		
	}

	@Override
	public long nextExecutionTime() {
		return this.cron.next(new Date()).getTime();
	}

}
