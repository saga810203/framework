package org.jfw.util.scheduler.container;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

//import org.jfw.log.LogFactroy;
//import org.jfw.log.Logger;
import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.JobInfo;
import org.jfw.util.scheduler.container.BaseJobContainer.Command;
import org.jfw.util.scheduler.impl.JobWorkerThreadPool;
//import org.jfw.utils.DateUtils;

public class BaseJobContainerThread extends Thread
{
//	private static final Logger	             log	                     = LogFactroy.getLog(BaseJobContainerThread.class);
	public static final String	             T_STOP	                     = "STOP";
	private boolean	                         isRunning	                 = true;
	private long	                         jobSchedulerInterval	     = 1L;

	private LinkedList<Command>	             allCommand	                 = new LinkedList<Command>();
	private LinkedList<JobExecutor>	         runningJobExecutors	     = new LinkedList<JobExecutor>();

	private SortedMap<Long, LinkedList<Job>>	waitQueueForMultipleJobs	= new TreeMap<Long, LinkedList<Job>>();

	private SortedMap<Long, LinkedList<Job>>	waitQueueForSingleJobs	 = new TreeMap<Long, LinkedList<Job>>();

	private JobWorkerThreadPool	             threadPool	                 = new JobWorkerThreadPool();

	private void addJobToWaitQueue(Job job, boolean isFirst)
	{
		JobInfo ji = job.getJobInfo();
		Long key = isFirst ? new Long(ji.getFirstRunningTime()) : new Long(ji.getNextRunningTime());
		SortedMap<Long, LinkedList<Job>> target = this.waitQueueForSingleJobs;
		if (ji.isMoreRunningJob())
			target = this.waitQueueForMultipleJobs;
		LinkedList<Job> list = target.get(key);
		if (null == list)
			list = new LinkedList<Job>();
		if (0 > list.indexOf(job))
		{
			list.add(job);
//			log.debug("Job[id=" + job.getId() + "]加入等待调度队列中，下次执行时间为：" + DateUtils.formatTimeStamp(key.longValue()));
		}
		target.put(key, list);
	}

	private boolean	isMultipleJobs	= true;

	private SortedMap<Long, LinkedList<Job>> getNextWaitQueue()
	{
		SortedMap<Long, LinkedList<Job>> target = this.waitQueueForSingleJobs;
		if (isMultipleJobs)
			target = this.waitQueueForMultipleJobs;
		return target;
	}

	// TODO:此算法可独立为一个接口
	private void allocateJob()
	{
		long currentTime = System.currentTimeMillis();
		boolean isHasTwoQueue = true;
		SortedMap<Long, LinkedList<Job>> target = null;
		LinkedList<Job> list = null;
		while (true)
		{
			if (isHasTwoQueue)
				target = this.getNextWaitQueue();
			Long key = target.firstKey();
			if (null == key || key.longValue() > currentTime)
			{
				if (isHasTwoQueue)
				{
					target = this.getNextWaitQueue();
					isHasTwoQueue = false;
					continue;
				} else
				{
					this.threadPool.releaseThread();
					break;
				}
			} else
			{
				list = target.get(key);
				this.allocateJob(list);
				if (list.size() > 0)
				{
					target.put(key, list);
					break;
				} else
				{
					target.remove(key);
				}
			}
		}
	}

	private void allocateJob(LinkedList<Job> list)
	{
		LinkedList<JobExecutor> jes = this.threadPool.handle(list);
		if (jes == null || jes.size() == 0)
			return;
		for (ListIterator<JobExecutor> it = jes.listIterator(); it.hasNext();)
		{

			JobExecutor je = it.next();
			Job job = je.getJob();
			JobInfo ji = job.getJobInfo();
//			log.debug("JOB[id=" + job.getId() + "]分配了工作线程");
			if (ji.isMoreRunningJob())
			{
				this.addJobToWaitQueue(job, false);
			}
			// JobSchedulerMode jsm = ji.getJobSchedulerMode();
			// if (jsm == JobSchedulerMode.FixedTime || jsm ==
			// JobSchedulerMode.IntervalByBegin)
			// {
			// this.addJobToWaitQueue(job, false);
			// }
		}
		this.runningJobExecutors.addAll(jes);
	}

	private void add(Job job)
	{
		this.addJobToWaitQueue(job, true);
	}

	private void interruptJob(String id)
	{
		for (ListIterator<JobExecutor> it = this.runningJobExecutors.listIterator(); it.hasNext();)
		{
			JobExecutor je = it.next();
			if (id.equals(je.getJob().getId()))
			{
				je.cancleNow();
				it.remove();
//				log.debug("JOB[id=" + je.getJob().getId() + "]被取消执行");
			}
		}
	}

	private void removeJobFromWaitQueue(Job job)
	{
//		log.debug("将JOB[id=" + job.getId() + "]从调度队列中移除");
		SortedMap<Long, LinkedList<Job>> target = this.waitQueueForSingleJobs;
		if (job.getJobInfo().isMoreRunningJob())
			target = this.waitQueueForMultipleJobs;
		LinkedList<Long> delList = new LinkedList<Long>();

		for (Map.Entry<Long, LinkedList<Job>> entry : target.entrySet())
		{
			Long key = entry.getKey();
			LinkedList<Job> val = entry.getValue();
			val.remove(job);
			if (val.isEmpty())
				delList.add(key);
		}
		for (Long ll : delList)
		{
			target.remove(ll);
		}
	}

	synchronized private void remove(Job job)
	{
		interruptJob(job.getId());
		this.removeJobFromWaitQueue(job);
//		log.debug("JOB[id=" + job.getId() + "]从工作线程中移除");
	}

	public void addCommand(String shell, Object paramter)
	{
		Command c = new Command();
		c.shell = shell;
		c.paramter = paramter;
		synchronized (allCommand)
		{
			this.allCommand.addLast(c);
		}
	}

	private Command takeCommand()
	{
		synchronized (allCommand)
		{
			if (allCommand.size() > 0)
				return allCommand.removeFirst();
		}
		return null;
	}

	private void handleCommands()
	{
		Command c = null;

		while ((c = this.takeCommand()) != null)
		{
			try
			{
				String cm = c.shell;
				if (cm.equals(BaseJobContainer.C_ADDJOB))
				{
					if ((c.paramter != null) && (c.paramter instanceof Job))
					{
						this.add((Job) c.paramter);
					}
				} else if (cm.equals(BaseJobContainer.C_REMOVEJOB))
				{
					if ((c.paramter != null) && (c.paramter instanceof Job))
					{
						this.remove((Job) c.paramter);
					}
				} else if (cm.equals(BaseJobContainer.C_SETSCHEDULERINTERVAL))
				{
					if ((c.paramter != null) && (c.paramter instanceof Long))
					{
						this.jobSchedulerInterval = ((Long) c.paramter).longValue();
					}
				} else if (cm.equals(BaseJobContainer.C_SETMAXIDLETIME))
				{
					if ((c.paramter != null) && (c.paramter instanceof Long))
					{
						this.threadPool.setMaxIdleTime(((Long) c.paramter).longValue());
					}
				} else if (cm.equals(BaseJobContainer.C_SETMAXWORKER))
				{
					if ((c.paramter != null) && (c.paramter instanceof Integer))
					{
						this.threadPool.setMaxSize(((Integer) c.paramter).intValue());
					}
				} else if (cm.equals(BaseJobContainer.C_SETMINWORKER))
				{
					if ((c.paramter != null) && (c.paramter instanceof Integer))
					{
						this.threadPool.setMinSize(((Integer) c.paramter).intValue());
					}
				} else if (cm.equals(T_STOP))
				{
					this.isRunning = false;
					break;
				}
			} catch (Exception e)
			{
//				log.error("在处理命令[command=" + c.shell + ";parameter=" + c.paramter.toString() + "]", e);
			}
		}
	}

	private void handleJobExecutors()
	{
		for (ListIterator<JobExecutor> it = this.runningJobExecutors.listIterator(); it.hasNext();)
		{
			JobExecutor je = it.next();
			if (je.isCompleted())
			{
				Job job = je.getJob();
				JobInfo ji = job.getJobInfo();
//				log.debug("JOB[id=" + job.getId() + "]上一次执行结束");
				// if(ji.getJobSchedulerMode() ==
				// JobSchedulerMode.IntervalByEnd)
				if (!ji.isMoreRunningJob())
				{
					this.addJobToWaitQueue(job, false);
				}
				it.remove();
			}
		}
	}

	@Override
	public void run()
	{
		try
		{
			while (this.isRunning)
			{
				handleCommands();
				if (!this.isRunning)
					break;
				handleJobExecutors();
				if (!this.isRunning)
					break;
				allocateJob();
				try
				{
					sleep(this.jobSchedulerInterval);
				} catch (InterruptedException e)
				{
				}
			}
		} finally
		{
			try
			{
				for (ListIterator<JobExecutor> it = this.runningJobExecutors.listIterator(); it.hasNext();)
				{
					JobExecutor je = it.next();
					if (!je.isCompleted())
					{
						je.cancleNow();
					}
				}
				this.runningJobExecutors.clear();
				this.runningJobExecutors = null;
				this.threadPool.releaseAll();
				this.threadPool = null;
				this.waitQueueForMultipleJobs.clear();
				this.waitQueueForMultipleJobs = null;
				this.waitQueueForSingleJobs.clear();
				this.waitQueueForSingleJobs = null;
				this.allCommand.clear();
				this.allCommand = null;
			} catch (Exception e)
			{
			}
		}

	}

}
