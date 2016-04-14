package org.jfw.util.scheduler.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.jfw.util.scheduler.ExecutedInfo;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.JobInfo;

public class JobWorkerThread extends Thread
{
	private volatile boolean	         isCancled	   = false;
	private ExecutedInfo	             ei;
	private AtomicReference<JobExecutor>	arjob	   = new AtomicReference<JobExecutor>();

	private volatile long	             beginIdleTime	= System.currentTimeMillis();

	private Object	                     waitLock	   = new Object();

	public boolean isInWorking()
	{
		return null != arjob.get();
	}

	public long idleTimes()
	{
		if (isInWorking())
			return 0;
		return System.currentTimeMillis() - this.beginIdleTime;
	}

	public boolean handle(JobInfo pJi, JobExecutor pJe, ExecutedInfo pEi)
	{
		boolean result = this.arjob.compareAndSet(null, pJe);
		if (result)
		{
			this.ei = pEi;
			this.waitLock.notifyAll();
		}
		return result;
	}

	public void release()
	{
		this.isCancled = true;
		this.interrupt();
	}

	@Override
	public void run()
	{
		while (!isCancled)
		{
			JobExecutor je = arjob.get();
			if (je == null)
			{
				this.beginIdleTime = System.currentTimeMillis();
				try
				{
					this.waitLock.wait();
				} catch (InterruptedException e)
				{
				}
			} else
			{
				try
				{
					try
					{
						je.execute(this.ei);
					} catch (Exception e)
					{
						this.ei.terminateFail("在JOb[id=" + je.getJob().getId() + "]的执行器中抛出了未处理的异常["
						        + e.getClass().getName() + "]", e);
					}
					this.ei.setEndTime(System.currentTimeMillis());
					je.getJob().getJobInfo().updateJobInfoAfterExecuted(this.ei);
					je.finalizeAtBeforeDisassociateThread();
				} finally
				{
					this.arjob.set(null);
				}
			}

		}
	}

}
