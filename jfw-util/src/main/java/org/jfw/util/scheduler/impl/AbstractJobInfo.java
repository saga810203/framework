package org.jfw.util.scheduler.impl;

import org.jfw.util.scheduler.JobInfo;
import org.jfw.util.scheduler.JobSchedulerMode;
import org.jfw.util.scheduler.ReadableExecutedInfo;

public abstract class AbstractJobInfo implements JobInfo
{
	
	public AbstractJobInfo(String pId,String pName,String pDescrption,boolean pMoreRunning,JobSchedulerMode pJsm)
	{
		this.name = pName;
		this.jobId = pId;
		this.descrption = pDescrption;
		this.jsm = pJsm;
		this.moreRunning = pMoreRunning;
	}
	@Override
	synchronized public JobInfo cloneJobInfo()
    {
	   ReadOnlyJobInfo roji = new ReadOnlyJobInfo(this.jobId, this.name, this.descrption, this.moreRunning, this.jsm);
	   roji.actived = this.actived;
	   roji.errorReason = this.errorReason;
	   roji.firstBeginTime = this.firstBeginTime;
	   roji.firstEndTime = this.firstEndTime;
	   roji.lastBeginTime = this.lastBeginTime;
	   roji.lastEndTime = this.lastEndTime;
	   roji.lastErrorTime = this.lastErrorTime;
	   roji.lastHandleDataTime = this.lastHandleDataTime;
	   roji.lastThrowable = this.lastThrowable;
	   roji.nextRunningTime = this.nextRunningTime;
	   roji.noHandleDataReason = this.noHandleDataReason;
	   roji.noHandleDataTime = this.noHandleDataTime;
	   roji.numForFailrue = this.numForFailrue;
	   roji.numForHandleData = this.numForHandleData;
	   roji.numForRunning = this.numForRunning;
	   return roji;
	   
	   
    }
	protected String errorReason = null;
	@Override
    public String getLastErrorReason()
    {
	    return this.errorReason;
    }
	protected String noHandleDataReason = null;
	@Override
    public String getLastNoHandleDataReason()
    {
	    return this.noHandleDataReason;
    }

	protected long	nextRunningTime;

	@Override
	public long getNextRunningTime()
	{
		return this.nextRunningTime;
	}

	protected long	numForRunning	= 0;

	@Override
	public long getNumForRunning()
	{
		return this.numForRunning;
	}

	protected long	numForHandleData	= 0;

	@Override
	public long getNumForHandleData()
	{
		return this.numForHandleData;
	}

	protected long	numForFailrue	= 0;

	@Override
	public long getNumForFailrue()
	{
		return this.numForFailrue;
	}
    protected long firstEndTime =-1;
	protected long	lastEndTime	= -1;

	@Override
	public long getLastEndTime()
	{
		return 0;
	}

	protected long	lastHandleDataTime	= -1;

	@Override
	public long getLastHandleDataTime()
	{
		return this.lastHandleDataTime;
	}

	protected long	lastErrorTime	= -1;

	@Override
    synchronized public void updateJobInfoBeforeExecuted(ReadableExecutedInfo rei)
    {
		if(this.firstBeginTime == -1) this.firstBeginTime = rei.getBeginTime();
	    this.lastBeginTime = rei.getBeginTime();
	    this.updateNextRunningTime(true);	    
    }

	@Override
	public long getLastErrorTime()
	{
		return this.lastErrorTime;
	}

	protected Throwable	lastThrowable	= null;

	@Override
	public Throwable getLastThrowable()
	{
		return this.lastThrowable;
	}

	private final String	jobId;

	@Override
	public String getId()
	{
		return this.jobId;
	}

	private final String	name;

	@Override
	public String getName()
	{
		return this.name;
	}

	protected boolean	actived	= true;

	@Override
	public boolean isActived()
	{
		return this.actived;
	}

	@Override
    public void active(boolean isActived)
    {
	    this.actived = isActived;
	    
    }
	private final String	descrption;

	@Override
	public String getDescrption()
	{
		return this.descrption;
	}

	@Override
	synchronized public void updateJobInfoAfterExecuted(ReadableExecutedInfo rei)
	{
		if(this.firstEndTime == -1) this.firstEndTime = rei.getEndTime();
		this.lastEndTime = rei.getEndTime();
		if(this.lastEndTime == -1) this.lastEndTime = System.currentTimeMillis();
		++numForRunning;
		if (!rei.isSuccessTermination())
		{
			++numForFailrue;
			this.lastErrorTime =this.lastEndTime;
			this.lastThrowable = rei.getFailureException();
			this.errorReason = rei.getFailureReason();
		}
		if (rei.isHanlded()){
			++numForHandleData;
			this.lastHandleDataTime = this.lastEndTime;
		}else
		{
			this.noHandleDataReason =rei.getNotHandledReason();
			this.noHandleDataTime = this.lastEndTime;
		}
		updateNextRunningTime(false);
	}
	private final boolean  moreRunning;
	@Override
    public boolean isMoreRunningJob()
	{
		return this.moreRunning;
	}

	private final JobSchedulerMode jsm;
	@Override
    public JobSchedulerMode getJobSchedulerMode()
    {
	    return this.jsm;
    }
	protected long noHandleDataTime;
	@Override
    public long getLastNoHandleDataTime()
    {
	    return this.noHandleDataTime;
    }
	
	protected long firstBeginTime = -1;
	protected long	lastBeginTime	= -1;

	@Override
	public long getLastBeginTime()
	{
		return this.lastBeginTime;
	}

	protected abstract void updateNextRunningTime(boolean isBeforExecuted);

}
