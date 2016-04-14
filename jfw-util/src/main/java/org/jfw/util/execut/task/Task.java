package org.jfw.util.execut.task;

public interface Task extends Runnable{
	boolean isScheduling();
	boolean isRunning();
	
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
	 * 最后一次异常发生时间（-1：从未发生过异常;）
	 */

	long getLastFailrueTime();

	/*
	 * 最后一次异常
	 */
	Throwable getLastThrowable();
	/**
	 * 中断
	 */	
	void interrupt();
}
