package org.jfw.util.scheduler.impl;

import org.jfw.util.scheduler.JobInfo;
import org.jfw.util.scheduler.JobSchedulerMode;
import org.jfw.util.scheduler.ReadableExecutedInfo;

public class ReadOnlyJobInfo extends AbstractJobInfo
{

	public ReadOnlyJobInfo(String pId, String pName, String pDescrption, boolean pMoreRunning, JobSchedulerMode pJsm)
    {
	    super(pId, pName, pDescrption, pMoreRunning, pJsm);
    }

	@Override
    public JobInfo cloneJobInfo()
    {
	    throw new UnsupportedOperationException("只读类，不支持此方法[ReadOnlyJobInfo.cloneJobInfo()]的调用");
    }

	@Override
    public void updateJobInfoBeforeExecuted(ReadableExecutedInfo rei)
    {
		throw new UnsupportedOperationException("只读类，不支持此方法[ReadOnlyJobInfo.updateJobInfoBeforeExecuted(ReadableExecutedInfo)]的调用");
    }

	@Override
    public void active(boolean isActived)
    {
		throw new UnsupportedOperationException("只读类，不支持此方法[ReadOnlyJobInfo.active(boolean)]的调用");
    }

	@Override
    public void updateJobInfoAfterExecuted(ReadableExecutedInfo rei)
    {
		throw new UnsupportedOperationException("只读类，不支持此方法[ReadOnlyJobInfo.updateJobInfoAfterExecuted(ReadableExecutedInfo)]的调用");
    }

	@Override
    public long getFirstRunningTime()
    {
	   return this.firstBeginTime;
    }

	@Override
    protected void updateNextRunningTime(boolean isBeforExecuted)
    {	    
    }



}
