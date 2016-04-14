package org.jfw.util.scheduler.cron;

public class NoDelayCronJobInfo extends CronJobInfo
{

	public NoDelayCronJobInfo(String pId, String pName, String pDescrption, boolean pMoreRunning, CronDate pCronDate)
    {
	    super(pId, pName, pDescrption, pMoreRunning, pCronDate);
    }
	
	@Override
	protected void updateNextRunning()
	{
		this.nextRunningTime = this.cronDate.getNextTime();
		long c = System.currentTimeMillis();
		if(c > this.nextRunningTime) this.nextRunningTime = this.cronDate.getNextTimeByTime(c);
	}

}
