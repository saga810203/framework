package org.jfw.util.scheduler;

public interface Job
{
	String getId();
	String getName();
	String getDescription();
	JobExecutor getJobExecutor();
	JobInfo getJobInfo();
}
