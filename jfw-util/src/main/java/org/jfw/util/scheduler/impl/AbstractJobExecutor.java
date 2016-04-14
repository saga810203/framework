package org.jfw.util.scheduler.impl;

import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicReference;

import org.jfw.util.log.LogFactory;
import org.jfw.util.log.Logger;
import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.WritableExecutedInfo;

public abstract class  AbstractJobExecutor implements JobExecutor
{
	private static final Logger log = LogFactory.getLog(AbstractJobExecutor.class);
	
	protected volatile boolean isCancled = false;
	protected String noHandleDataReason = null;
	protected String failrueReason = null;
	protected Job job;
	protected AtomicReference<Thread> runningThread= new AtomicReference<Thread>();
	
	
	private String  logJobInfo;
	
	public AbstractJobExecutor(){}
	
	
	public AbstractJobExecutor(Job pJob)
	{
		this.job = pJob;
		this.logJobInfo ="执行Job[id="+job.getId()+"]:";
	}
	public void setJob(Job pJob)
	{
		this.job = pJob;
		this.logJobInfo ="执行Job[id="+job.getId()+"]:";	
	}
	
	@Override
    public Job getJob()
    {
	    return this.job;
    }

	protected String logFormat(String msg)
	{
		return this.logJobInfo+msg;
	}

	@Override
    public void initAtBeforeAssociateThread(Thread thread)
    {
	   this.runningThread.set(thread); 
	   this.isCancled = false;
    }


	@Override
    public void finalizeAtBeforeDisassociateThread()
    {
	    this.runningThread.set(null);
	    
    }

	@Override
    public boolean isCompleted()
    {
		return this.runningThread.get() != null;
    }
	
	@Override
    public void execute(WritableExecutedInfo wei)
	{
		if(log.isEnableTrace())log.trace(logFormat("开始"));
		if(!isHandleData()){
			wei.notHandle(this.noHandleDataReason);
			if(log.isEnableTrace())log.trace(logFormat("没有处理数据返回"));
		}else
		{
		    if(log.isEnableTrace())log.trace(logFormat("开始处理数据"));
			try{
				handle();
				if(log.isEnableTrace())log.trace(logFormat("处理数据正常完成"));
			}catch(InterruptedException e)
			{
				if(this.isCancled){ 
				    if(log.isEnableTrace())log.trace(logFormat("Job被取消了"));
					return;
				}
				if(log.isEnableTrace())log.error(logFormat("出现异常："+e.getClass().getName()),e);
				wei.terminateFail(this.failrueReason, e);
			}catch(InterruptedIOException e)
			{
				if(this.isCancled){ 
				    if(log.isEnableTrace())log.trace(logFormat("Job被取消了"));
					return;
				}
				if(log.isEnableTrace())log.error(logFormat("出现异常："+e.getClass().getName()),e);
				wei.terminateFail(this.failrueReason, e);
			}catch(Exception e)
			{
			    if(log.isEnableTrace())log.error(logFormat("出现异常："+e.getClass().getName()),e);			
				wei.terminateFail(this.failrueReason, e);
			}
		}
	}

	public boolean isHandleData(){
		return true;
	}
	
	public abstract void handle()throws Exception; 


	@Override
   synchronized public void cancleNow()
    {
		if(this.isCancled) return;
	    this.isCancled = true;
	    Thread ct = this.runningThread.getAndSet(null);
	    if(ct!= null)ct.interrupt();
    }

	@Override
	synchronized public void cancle()
    {
		if(this.isCancled) return;
	    this.isCancled = true;
    }
}
