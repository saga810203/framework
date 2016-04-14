package org.jfw.util.scheduler.impl;

import java.util.LinkedList;
import java.util.ListIterator;

import org.jfw.util.scheduler.ExecutedInfo;
import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.JobInfo;

public class JobWorkerThreadPool
{
	public static final int DEFAULT_MAX_SIZE = 10;
	public static final long DEFAULT_MAX_IDLE_TIME = 30*1000L;
	public static final int DEFAULT_MIN_SIZE = 2;
	private LinkedList<JobWorkerThread> cache = new LinkedList<JobWorkerThread>();
	
	private volatile int maxSize = DEFAULT_MAX_SIZE;
	private volatile long maxIdleTime = DEFAULT_MAX_IDLE_TIME;
	private volatile int minSize = DEFAULT_MIN_SIZE;
	
	
	private LinkedList<JobWorkerThread> idles = new LinkedList<JobWorkerThread>();
	
	public int getMaxSize()
	{
		return maxSize;
	}
	public void setMaxSize(int maxSize)
	{
		maxSize = maxSize<=0?DEFAULT_MAX_SIZE:maxSize;
		this.maxSize = maxSize;
	}
	public long getMaxIdleTime()
	{
		return maxIdleTime;
	}
	public void setMaxIdleTime(long maxIdleTime)
	{
		maxIdleTime = maxIdleTime<=0?DEFAULT_MAX_IDLE_TIME:maxIdleTime;
		this.maxIdleTime = maxIdleTime;
	}
	public int getMinSize()
	{
		return minSize;
	}
	public void setMinSize(int minSize)
	{
		minSize = minSize<=0?DEFAULT_MIN_SIZE:minSize;
		this.minSize = minSize;
	}
	
	
	

	
	private void refresh()
	{
		this.idles.clear();
		ListIterator<JobWorkerThread> it = this.cache.listIterator();	
		while(it.hasNext())
		{
			JobWorkerThread jwt = it.next();
			if(!jwt.isInWorking())this.idles.add(jwt);			
		}
	}

	private JobExecutor handle(Job job)
	{
		if(this.idles.size()> 0) 
		{
			JobWorkerThread jwt = this.idles.getFirst();
			this.idles.removeFirst();
			ExecutedInfo ei = new BaseExecutedInfo();
			JobInfo ji =job.getJobInfo();
			JobExecutor je = job.getJobExecutor();
			
			ji.updateJobInfoBeforeExecuted(ei);
			je.initAtBeforeAssociateThread(jwt);
			jwt.handle(ji, je, ei);
			
			return je;
		}
		if(this.cache.size()< this.maxSize){
			JobWorkerThread jwt= new JobWorkerThread();
			jwt.start();
			ExecutedInfo ei = new BaseExecutedInfo();
			JobInfo ji =job.getJobInfo();
			JobExecutor je = job.getJobExecutor();		
			ji.updateJobInfoBeforeExecuted(ei);
			je.initAtBeforeAssociateThread(jwt);
			jwt.handle(ji, je, ei);
			this.cache.add(jwt);
			return je;
		} 
		return null;		
	}
	
	public void releaseThread()
	{
		//TODO:算法没有公平性，不能保证空闲时间长的线程被释放
		if(this.cache.size() <= this.minSize) return;
		
		int releaseCount = Math.min(this.idles.size(),this.cache.size() - this.minSize);
		
		ListIterator<JobWorkerThread> it = this.idles.listIterator();	
		while(it.hasNext() && releaseCount > 0)
		{
			JobWorkerThread jwt = it.next();
			if(jwt.idleTimes() > this.maxIdleTime){
				this.cache.remove(jwt);
				jwt.release();
				--releaseCount;
			}			
		}		
		
	}
	
	public void releaseAll()
	{
		for(JobWorkerThread jwt : this.cache)
		{
			jwt.release();
		}
	}
	
	public LinkedList<JobExecutor> handle(LinkedList<Job> list)
	{
		LinkedList<JobExecutor> result = new LinkedList<JobExecutor>();
		this.refresh();
	  //TODO:算法没有公平性，不能保证空闲时间长的线程被调用（没有按资排辈）
	    while(list.size()> 0)
	    {
	    	JobExecutor je =this.handle(list.getFirst());
	    	if(je == null)
	    	{
	    		break;
	    	}else{
	    		result.add(new ThreadWrapperJobExecutor(je));
	    		list.removeFirst();
	    	}
	    }
	    return result;
	}
	
	
}
