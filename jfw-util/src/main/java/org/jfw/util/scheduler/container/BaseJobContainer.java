package org.jfw.util.scheduler.container;

import java.util.HashMap;
import java.util.Map;

import org.jfw.util.log.LogFactory;
import org.jfw.util.log.Logger;
import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobInfo;

public class BaseJobContainer implements JobContainer
{

	private static final Logger	log	= LogFactory.getLog(BaseJobContainer.class);

//	public static class JobContainerInfo implements ServiceInfo
//	{
//
//		private long		           staredTime	= -1;
//		private long		           stopedTime	= -1;
//		private volatile ServiceStatus	statu		= ServiceStatus.Created;
//
//		@Override
//		public long getStartedTime()
//		{
//			return this.staredTime;
//		}
//
//		@Override
//		public long getStopedTime()
//		{
//			return this.stopedTime;
//		}
//
//		@Override
//		public ServiceStatus getStatu()
//		{
//			return this.statu;
//		}
//	}

	public static final String	   C_SETSCHEDULERINTERVAL	= "C";
	public static final String	   C_ADDJOB	              = "A";
	public static final String	   C_REMOVEJOB	          = "R";
	public static final String	   C_SETMAXWORKER	      = "MXW";
	public static final String	   C_SETMINWORKER	      = "MIW";
	public static final String	   C_SETMAXIDLETIME	      = "MXI";

//	private JobContainerInfo	   si	                  = new JobContainerInfo();

	/*
	 * 所有job
	 */
	private Map<String, Job>	   allJob	              = new HashMap<String, Job>();

	private BaseJobContainerThread	mainThread;
	private long	               maxIdleTime	          = 30 * 1000L;
	private int	                   maxWorkerThread	      = 5;
	private int	                   minWorkerThread	      = 0;
	private long	               jobSchedulerInterval	  = 30 * 1000L;

//	@Override
	synchronized public void start()
	{
		log.info("启动BaseJobContainer类实例：开始");
		this.mainThread = new BaseJobContainerThread();

		this.mainThread.addCommand(C_SETMAXIDLETIME, new Long(this.maxIdleTime));
		this.mainThread.addCommand(C_SETMAXWORKER, new Integer(this.maxWorkerThread));
		this.mainThread.addCommand(C_SETMINWORKER, new Integer(this.minWorkerThread));
		this.mainThread.addCommand(C_SETSCHEDULERINTERVAL, new Long(this.jobSchedulerInterval));

		if (!this.allJob.isEmpty())
		{
			Job[] jobs = this.allJob.values().toArray(new Job[this.allJob.size()]);
			this.allJob.clear();
			for (Job job : jobs)
			{
				if (job.getJobInfo().isActived())
					mainThread.addCommand(C_ADDJOB, job);
			}

		}
		this.mainThread.start();
//		this.si.statu = ServiceStatus.Running;
//		this.si.staredTime = System.currentTimeMillis();
		log.info("启动BaseJobContainer类实例：结束");
	}

//	@Override
	synchronized public void stop()
	{
		log.info("停止BaseJobContainer类实例：开始");
		if (this.mainThread != null)
		{

			this.mainThread.addCommand(BaseJobContainerThread.T_STOP, null);
			this.mainThread.interrupt();
			this.mainThread = null;
//			this.si.statu = ServiceStatus.Stoped;
//			this.si.stopedTime = System.currentTimeMillis();
		}
		log.info("停止BaseJobContainer类实例：结束");
	}

//	@Override
//	synchronized public ServiceInfo getServiceInfo()
//	{
//		return this.si;
//	}

//	@Override
//	synchronized public void init()
//	{
//		this.si.statu = ServiceStatus.Inited;
//	}

	@Override
	synchronized public JobContainer addJob(Job job)
	{

		Job oldJob = this.allJob.put(job.getId(), job);
		if (this.mainThread != null)
		{
			if (null != oldJob)
				this.mainThread.addCommand(C_REMOVEJOB, oldJob);
			if (job.getJobInfo().isActived())
			{
				this.mainThread.addCommand(C_ADDJOB, job);
			}
			this.mainThread.interrupt();
		}
		log.info("JOB[id=" + job.getId() + "]加入容器");
		return this;
	}

	@Override
	synchronized public JobContainer removeJob(String jobId)
	{
		Job job = this.allJob.remove(jobId);
		if (null != job)
		{
			if ((null != this.mainThread) && (job.getJobInfo().isActived()))
			{
				this.mainThread.addCommand(C_REMOVEJOB, job);
				this.mainThread.interrupt();
			}
			log.info("JOB[id=" + jobId + "]从容器中移除");
		}

		return this;
	}

	@Override
	synchronized public JobContainer pauseJob(String jobId)
	{
		Job job = this.allJob.get(jobId);
		if (job == null)
			return this;
		
		log.info("JOB[id="+jobId+"]被暂停执行");
		if (job.getJobInfo().isActived())
		{
			job.getJobInfo().active(false);
			if (this.mainThread != null)
				this.mainThread.addCommand(C_REMOVEJOB, job);
			this.mainThread.interrupt();
		}
		return this;
	}

	@Override
	synchronized public JobContainer changeIntervalByJobScheduler(long time)
	{
		time = 0 > time ? 0 : time;
		this.jobSchedulerInterval = time;
		if (this.mainThread != null)
		{
			this.mainThread.addCommand(C_SETSCHEDULERINTERVAL, new Long(this.jobSchedulerInterval));
			this.mainThread.interrupt();
		}
		return this;
	}

	@Override
	synchronized public JobContainer resumeJob(String jobId)
	{
		Job job = this.allJob.get(jobId);
		if (job == null)
			return this;
		log.info("JOB[id="+jobId+"]被恢复调度执行");
		if (!job.getJobInfo().isActived())
		{
			job.getJobInfo().active(true);
			if (this.mainThread != null)
			{
				this.mainThread.addCommand(C_ADDJOB, job);
				this.mainThread.interrupt();
			}

		}
		return this;
	}

	@Override
	synchronized public JobContainer setMaxWorker(int num)
	{
		num = num < 0 ? 5 : num;
		this.maxWorkerThread = num;
		if (this.mainThread != null)
		{
			this.mainThread.addCommand(C_SETMAXWORKER, new Integer(this.maxWorkerThread));
			this.mainThread.interrupt();
		}
		return this;
	}

	@Override
	synchronized public JobContainer setMinWorker(int num)
	{
		num = num < 0 ? 0 : num;
		this.minWorkerThread = num;
		if (this.mainThread != null)
		{
			this.mainThread.addCommand(C_SETMINWORKER, new Integer(this.minWorkerThread));
			this.mainThread.interrupt();
		}
		return this;
	}

	@Override
	synchronized public JobContainer setMaxIdieTime(long time)
	{
		time = time < 0 ? 0 : time;
		this.maxIdleTime = time;
		if (this.mainThread != null)
		{
			this.mainThread.addCommand(C_SETMAXIDLETIME, new Long(this.maxIdleTime));
			this.mainThread.interrupt();
		}
		return this;
	}

	@Override
	synchronized public JobInfo[] getAllJobInfo()
	{
		if (this.allJob.size() == 0)
			return null;
		JobInfo[] result = new JobInfo[allJob.size()];
		Job[] jobs = allJob.values().toArray(new Job[allJob.size()]);

		for (int i = 0; i < jobs.length; ++i)
		{
			result[i] = jobs[i].getJobInfo().cloneJobInfo();
		}
		return result;
	}

	@Override
	synchronized public JobInfo getJobInfoById(String id)
	{
		Job job = this.allJob.get(id);
		return null == job ? null : job.getJobInfo().cloneJobInfo();
	}

	public static class Command
	{
		public String	shell;
		public Object	paramter;
	}

}
