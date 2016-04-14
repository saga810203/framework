package org.jfw.util.scheduler;

public enum JobSchedulerMode {
	//定时执行
	FixedTime,
	//以开发时间为计算依据的间隔执行
	IntervalByBegin,
	//以结束时间为计算依据的间隔执行
	IntervalByEnd

}
