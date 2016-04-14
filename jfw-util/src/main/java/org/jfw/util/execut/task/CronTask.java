package org.jfw.util.execut.task;

public abstract class CronTask extends AbstractTask {
	private String cronString;
	private CronDate cronDate;
	
	protected CronTask(){
		this.nextRunningTime = Long.MAX_VALUE;
	}
	public void setCronString(String cronString) {
		this.cronString = cronString;
		this.cronDate = CronDate.buildCronDate(this.cronString);
		this.resetNextRunningTime();
	}
	public void setCron(String cron) {
		this.cronString = cron;
	}
	@Override
	protected void updateNextRunningTime() {
		if(this.cronDate!=null)
			this.nextRunningTime = cronDate.getNextTime();
	}
	@Override
	protected void resetNextRunningTime() {
		if(this.cronDate!=null){
			this.nextRunningTime = cronDate.getNextTimeByTime(System.currentTimeMillis());
		}
	}
}
