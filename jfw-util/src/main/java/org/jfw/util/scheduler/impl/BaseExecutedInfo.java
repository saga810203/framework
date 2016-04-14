package org.jfw.util.scheduler.impl;

import org.jfw.util.scheduler.ExecutedInfo;

public class BaseExecutedInfo implements ExecutedInfo
{
	private long	  beginTime	         = System.currentTimeMillis();
	private long	  endTime	         = -1;
	private boolean	  resultSuccess	     = true;
	private boolean	  isHandleData	     = true;
	private Throwable	runningThrowable	= null;
	private String failrueReason = null;
	private String notHandleDataReason = null;


	public void setBeginTime(long time)
	{
		this.beginTime = time;
	}


	public void setEndTime(long time)
	{
		this.endTime = time;

	}

	@Override
	public void terminateFail(String reason, Throwable t)
	{
		this.resultSuccess = false;
		this.runningThrowable = t;
		this.failrueReason = reason;

	}

	@Override
	public void notHandle(String reason)
	{
		this.isHandleData = false;
		this.notHandleDataReason = reason;
	}

	@Override
    public boolean isHanlded()
    {
	    return this.isHandleData;
    }

	@Override
    public boolean isSuccessTermination()
    {
	    
	    return this.resultSuccess;
    }

	@Override
    public Throwable getFailureException()
    {
	   
	    return this.runningThrowable;
    }

	@Override
    public String getFailureReason()
    {
	   
	    return this.failrueReason;
    }

	@Override
    public String getNotHandledReason()
    {
	    return this.notHandleDataReason;
    }

	@Override
    public long getBeginTime()
    {
	    
	    return this.beginTime;
    }

	@Override
    public long getEndTime()
    {
	    return this.endTime;
    }

}
