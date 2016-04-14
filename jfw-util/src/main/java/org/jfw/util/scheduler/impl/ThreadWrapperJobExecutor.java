package org.jfw.util.scheduler.impl;

import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.WritableExecutedInfo;

public class ThreadWrapperJobExecutor implements JobExecutor
{
	
	private final JobExecutor je;
	
	public ThreadWrapperJobExecutor(JobExecutor pJe)
	{
		this.je = pJe;

	}

	@Override
	public void initAtBeforeAssociateThread(Thread thread)
	{
		this.je.initAtBeforeAssociateThread(thread);
	}

	@Override
	public void finalizeAtBeforeDisassociateThread()
	{
		this.je.finalizeAtBeforeDisassociateThread();

	}

	@Override
	public void execute(WritableExecutedInfo wei)
	{
		this.je.execute(wei);
	}

	@Override
	public void cancle()
	{
		this.je.cancle();
	}

	@Override
	public void cancleNow()
	{
		this.je.cancleNow();
	}

	@Override
	public boolean isCompleted()
	{
		
		return this.je.isCompleted();
	}

	@Override
	public Job getJob()
	{
		
		return this.je.getJob();
	}


	@Override
    public void setJob(Job pJob)
    {
	   throw new UnsupportedOperationException("不能执行此方法[ThreadWrapperJobExecutor.setJob(Job)]");
	    
    }
	

}
