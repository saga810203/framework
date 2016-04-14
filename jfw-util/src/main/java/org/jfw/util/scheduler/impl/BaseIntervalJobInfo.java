package org.jfw.util.scheduler.impl;

import org.jfw.util.scheduler.JobSchedulerMode;
/*
 * 
 * pIntervalTime 单位为毫秒 
 */
public class BaseIntervalJobInfo extends AbstractJobInfo
{
	
	private final long intervalTime;
	public BaseIntervalJobInfo(String pId,String pName,String pDescrption,boolean pMoreRunning,boolean isIntervalByBegin,long pIntervalTime)
	{
		super(pId, pName, pDescrption, pMoreRunning,isIntervalByBegin?JobSchedulerMode.IntervalByBegin:JobSchedulerMode.IntervalByEnd);
		this.intervalTime = 0>pIntervalTime?0:pIntervalTime;
	}
	
	
	@Override
    protected void updateNextRunningTime(boolean isBeforExecuted)
    {
		
	    if(isBeforExecuted && (this.getJobSchedulerMode() == JobSchedulerMode.IntervalByBegin))
	    {
	    	this.nextRunningTime = this.lastBeginTime + this.intervalTime;
	    }else if((!isBeforExecuted) && (this.getJobSchedulerMode() == JobSchedulerMode.IntervalByEnd))
	    {
	    	this.nextRunningTime = this.lastEndTime + this.intervalTime;
	    }
	    
    }


	@Override
    public long getFirstRunningTime()
    {
	    return System.currentTimeMillis();
    }

}
