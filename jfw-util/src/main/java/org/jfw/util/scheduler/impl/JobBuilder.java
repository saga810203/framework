package org.jfw.util.scheduler.impl;

import java.util.HashMap;
import java.util.Map;

import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.JobInfo;
import org.jfw.util.scheduler.JobSchedulerMode;
import org.jfw.util.scheduler.cron.CronDate;
import org.jfw.util.scheduler.cron.CronJobInfo;

public class JobBuilder
{
	private JobBuilder()
	{
	};

	public static JobBuilder create()
	{
		return new JobBuilder();
	}

	private String	jobId;

	public JobBuilder setJobId(String pJobId)
	{
		this.jobId = pJobId;
		return this;
	}

	private String	jobName;

	public JobBuilder setJobName(String pJobName)
	{
		this.jobName = pJobName;
		return this;
	}

	private String	jobDescription;

	public JobBuilder setJobDescription(String pJobDescription)
	{
		this.jobDescription = pJobDescription;
		return this;
	}

	private boolean	moreRunning	= false;

	public JobBuilder setMoreRunning()
	{
		this.moreRunning = true;
		return this;
	}

	private boolean	actived	= false;

	public JobBuilder setActived()
	{
		this.actived = true;
		return this;
	}

	private JobSchedulerMode	jsm;

	public JobBuilder setJobSchedulerMode(JobSchedulerMode pJsm)
	{
		this.jsm = pJsm;
		return this;
	}

	private long	interval	= -1;

	public JobBuilder setRunInterval(long time)
	{
		if (0 > time)
			throw new IllegalArgumentException("任务运行间隔必须大于零");
		this.interval = time;
		return this;
	}

	private String	cronStr;

	public JobBuilder setCronString(String pCronStr)
	{
		this.cronStr = pCronStr;
		return this;
	}

	private String	jeClassName;

	public JobBuilder setJobExecutorClassName(String pClassName)
	{
		this.jeClassName = pClassName;
		return this;
	}

	private Class<?>	jeClass;

	public JobBuilder setJobExecutorClass(Class<?> pClass)
	{
		this.jeClass = pClass;
		return this;
	}
	
	private HashMap<String,String>  params = new HashMap<String, String>();
	
	public JobBuilder addParam(String key,String val)
	{
		this.params.put(key, val);
		return this;
	}

	private DefaultJob createJob() 
	{
		if (null ==this.jobId || this.jobId.trim().length()==0)
			throw new IllegalArgumentException("在使用JobBuilder构建Job时必须指定JobId");
		if (null==this.jobName|| this.jobName.trim().length()==0)
			throw new IllegalArgumentException("在使用JobBuilder构建Job时必须指定JobName");

		DefaultJob result = new DefaultJob(jobId, jobName, this.jobDescription == null ? "" : this.jobDescription);
	
		for(Map.Entry<String, String> entry:this.params.entrySet())
		{
			result.addParamter(entry.getKey(), entry.getValue());
		}
		
		return result;
	}

	private void createJobInfo(DefaultJob job)
	{
		JobInfo ji = null;
		if (this.jsm == null)
			throw new IllegalArgumentException("在使用JobBuilder构建Job时必须指定JobSchedulerMode");

		if (jsm == JobSchedulerMode.FixedTime)
		{
			if (null==this.cronStr||this.cronStr.trim().length()==0)
				throw new IllegalArgumentException("定时执行的任务必须指定定时执行字符串（分 小时 日 月 星期）");
			CronDate cd = CronDate.buildCronDate(this.cronStr);
			ji = new CronJobInfo(job.getId(), job.getName(), job.getDescription(), this.moreRunning, cd);

		} else
		{
			if (-1 == this.interval)
				throw new IllegalArgumentException("间隔执行的任务必须指定时间间隔（单位：毫秒）");

			ji = new BaseIntervalJobInfo(job.getId(), job.getName(), job.getDescription(), this.moreRunning,
			        this.jsm == JobSchedulerMode.IntervalByEnd, this.interval);
		}

		ji.active(this.actived);
		job.setJobInfo(ji);
	}

	@SuppressWarnings("unchecked")
    private void createJobExecutor(DefaultJob job) 
	{
		if (this.jeClass == null)
		{
			if (null==this.jeClassName||this.jeClassName.trim().length()==0)
			{
				throw new IllegalArgumentException("Job没有指定的执行类");
			}

			try
			{
				this.jeClass = Thread.currentThread().getContextClassLoader().loadClass(this.jeClassName);
			} catch (ClassNotFoundException e)
			{
				throw new IllegalArgumentException("无法为Job加载指定的类[" + this.jeClassName + "]");
			}

		}
		if (!JobExecutor.class.isAssignableFrom(this.jeClass))
		{
			throw new IllegalArgumentException("为Job指定的执行类[" + this.jeClass.getName() + "]没有实现JobExecutor接口");
		}
		JobExecutor je;
		try
		{
			 je= (JobExecutor) this.jeClass.newInstance();
		} catch (Exception e)
		{
			throw new IllegalArgumentException("为Job指定的执行类[" + this.jeClass.getName() + "]无法实例化（采无参构造方法）");
		}
		job.setJobExecutorClass((Class<JobExecutor>) this.jeClass);
		if(!this.moreRunning){
			je.setJob(job);
			job.setJobExecutor(je);
		}

	}

	public Job builder() 
	{
		DefaultJob job = this.createJob();
		this.createJobInfo(job);
		this.createJobExecutor(job);		
		return job;
	    
	}
}
