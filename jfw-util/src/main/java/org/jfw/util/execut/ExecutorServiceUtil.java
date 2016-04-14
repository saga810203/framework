package org.jfw.util.execut;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jfw.util.execut.task.Task;

public abstract class ExecutorServiceUtil {
    private static final ExecutorService es = Executors.newCachedThreadPool();
    
    public static void shutdown(){
    	es.shutdown();
    }
    
	public Task submit(String cronExpress,Runnable runnable){
		return null;
	}
	public Task submit(String cronExpress,Object obj,Method method,Method ininterruptMethod){
		return null;
	}
	public Task submitDelay(long delay,Runnable runnable){
		return null;
	}
	public Task submitDelay(long delay,Object obj,Method method,Method ininterruptMethod){
		return null;
	}
	public Task submitRate(long rate,Runnable runnable){
		return null;
	}
	public Task submitRate(long rate,Object obj,Method method,Method ininterruptMethod){
		return null;
	}
    
    
    
    
}
