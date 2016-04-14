package org.jfw.util.scheduler.impl;

import org.jfw.util.scheduler.JobSchedulerMode;


public abstract class AbstractFixedTimeJobInfo extends AbstractJobInfo
{

	public AbstractFixedTimeJobInfo(String pId,String pName,String pDescrption,boolean pMoreRunning)
	{
		super(pId, pName, pDescrption, pMoreRunning, JobSchedulerMode.FixedTime);
	}
	@Override
    protected void updateNextRunningTime(boolean isBeforExecuted)
    {
    }
	@Override
    synchronized public long getNextRunningTime()
    {
		updateNextRunning();
	    return this.nextRunningTime;
    }
	protected abstract void updateNextRunning();

	
}
