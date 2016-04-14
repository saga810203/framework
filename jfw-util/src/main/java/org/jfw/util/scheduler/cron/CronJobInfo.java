package org.jfw.util.scheduler.cron;

import org.jfw.util.scheduler.impl.AbstractFixedTimeJobInfo;

public class CronJobInfo extends AbstractFixedTimeJobInfo
{

	protected final CronDate cronDate;
	public CronJobInfo(String pId, String pName, String pDescrption, boolean pMoreRunning,CronDate pCronDate)
    {
	    super(pId, pName, pDescrption, pMoreRunning);
	    this.cronDate = pCronDate;
    }

	@Override
	synchronized public long getFirstRunningTime()
	{
		return this.cronDate.getNextTimeByTime(System.currentTimeMillis());
	}

	@Override
	protected void updateNextRunning()
	{
		this.nextRunningTime = this.cronDate.getNextTime();
	}

}
