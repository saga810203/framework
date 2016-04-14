package org.jfw.util.scheduler;

public interface ReadableExecutedInfo extends Cloneable
{
	long getBeginTime();
	long getEndTime();
	boolean isHanlded();
	boolean isSuccessTermination();
	Throwable getFailureException();
	String getFailureReason();
	String getNotHandledReason();	
}
