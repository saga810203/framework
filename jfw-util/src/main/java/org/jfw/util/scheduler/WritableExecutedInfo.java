package org.jfw.util.scheduler;

public interface WritableExecutedInfo
{
	void setEndTime(long time);
	void setBeginTime(long time);
	/*
	 * 设置Job未执行成功（业务处理出现异常）
	 */
	void terminateFail(String reason,Throwable t);
	/*
	 * 设置是Job本次执行未处理业务逻辑（原因）
	 */
	void notHandle(String reason);	
}
