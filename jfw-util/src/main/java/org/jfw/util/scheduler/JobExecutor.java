package org.jfw.util.scheduler;

public interface JobExecutor
{
	/*
	 *在关联线程对象前进行的初始化动作
	 */
	void initAtBeforeAssociateThread(Thread thread);	
	/*
	 *在取消关联线程对象前进行的动作
	 */
	void finalizeAtBeforeDisassociateThread();
	/*
	 * 执行的内容
	 */
	void execute(WritableExecutedInfo wei);
	/*
	 * 取消任务的执行(不使用中断)
	 */
	void cancle();
	/*
	 * 取消任务的执行(使用中断)
	 */	
	void cancleNow();
	/*
	 * 是否执行完成
	 */
	boolean isCompleted();
	/*
	 * 
	 */
	Job getJob();
	void setJob(Job pJob);
	
}
