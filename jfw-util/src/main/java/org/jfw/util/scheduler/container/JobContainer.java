package org.jfw.util.scheduler.container;

import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobInfo;

public interface JobContainer 
{
	/*
	 * 增加一个可可调度的Task
	 */
	JobContainer addJob(Job job);
	/*
	 * 移除一个Task;
	 */
	JobContainer removeJob(String jobId);

	
	/*
	 * 暂停一个Task的调度
	 */
	JobContainer pauseJob(String jobId);
	/*
	 * 重启一个Task的调度
	 */
	JobContainer resumeJob(String jobId);
	/*
	 * 设置最大的工作线程数（if num <1 then num = 5）
	 */
	JobContainer setMaxWorker(int num);
	/*
	 * 置最小的工作线程数（if num <0 then num =0）
	 */
	JobContainer setMinWorker(int num);
	/*
	 * 设置工作线程的最大空闲时间(毫秒)
	 */
	JobContainer setMaxIdieTime(long time);
	/*
	 * 获取所有Task的信息
	 */	
	JobInfo[] getAllJobInfo();
	/*
	 * 获取指定的Task的信息	
	 */
	JobInfo getJobInfoById(String id);
	JobContainer changeIntervalByJobScheduler(long time);
	
}
