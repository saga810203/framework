package org.jfw.util.scheduler;

public interface JobInfo 
{
	/*
	 * 是否可以在同一时间有多个线程运行Job
	 */
	boolean isMoreRunningJob();
	/*
	 * Job的调度模式
	 */	
	JobSchedulerMode getJobSchedulerMode();	
	/*
	 * 下次执行时间
	 */
	long getNextRunningTime();
	/*
	 * 第一次执行时间或（由暂停变为运行的第一次执行时间）
	 */
	long getFirstRunningTime();
	
	/*
	 * 运行次数
	 */
	long getNumForRunning();
	/*
	 * 处理数据次数
	 */
	long getNumForHandleData();
	/*
	 * 异常次数
	 */
	long getNumForFailrue();
	
	/*
	 * 最后一次运行开始时间（-1：从未运行过）
	 */
	long getLastBeginTime();	
	
	/*
	 * 最后一次运行结果时间（-1：从未运行过）
	 */
	long getLastEndTime();
	/*
	 * 最后一次处理业务时间（-1：从未处理过业务）
	 */
	long getLastHandleDataTime();
	/*
	 * 最后一次未处理业务时间（-1：从未执行过）
	 */
	long getLastNoHandleDataTime();
	/*
	 * 最后一次异常发生时间（-1：从未发生过异常;）
	 */

	long getLastErrorTime();
	/*
	 * 最后一次异常说明
	 */	
	String getLastErrorReason();
	/*
	 * 最后一次未处理数据说明
	 */
	String getLastNoHandleDataReason();
	/*
	 * 最后一次异常
	 */
	Throwable getLastThrowable();
	/*
	 * Id
	 */
	String getId();
	/*
	 * Name
	 */
	String getName();
	/*
	 * 是不是可以调度的(暂停的)
	 */
	boolean isActived();
	/*
	 * 此方法在Job放入容器中（由容器调度）只能通过容品还修改
	 */
	void active(boolean isActived);
	
	/*
	 * 说明
	 */
	String getDescrption();
	
	/*
	 * 根据任务执行情况更新TaskInfo
	 */
	void updateJobInfoBeforeExecuted(ReadableExecutedInfo rei);
	void updateJobInfoAfterExecuted(ReadableExecutedInfo rei);
	
	JobInfo cloneJobInfo();
	
}
