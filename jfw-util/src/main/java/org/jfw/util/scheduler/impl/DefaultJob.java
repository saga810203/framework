package org.jfw.util.scheduler.impl;

import java.util.LinkedHashMap;

import org.jfw.util.scheduler.Job;
import org.jfw.util.scheduler.JobExecutor;
import org.jfw.util.scheduler.JobInfo;

public class DefaultJob implements Job
{

	
	private final String id;
	private final String name;
	private final String descrption;
	private final LinkedHashMap<String, String> paramters = new LinkedHashMap<String,String>();

	
	public void addParamter(String key,String val)
	{
		this.paramters.put(key, val);
	}
	
	public String getParamter(String key)
	{
		return this.paramters.get(key);
	}
	public String[] getAllParamterNames()
	{
		return this.paramters.keySet().toArray(new String[this.paramters.size()]);
	}
	public void setJobInfo(JobInfo ji)
	{
		this.ji = ji;
	}

	public void setJobExecutor(JobExecutor je)
	{
		this.je = je;
	}



	private JobInfo ji;
	private JobExecutor je;
	private Class<JobExecutor> clazz;
	
	
	
	public void setJobExecutorClass(Class<JobExecutor> clazz)
	{
		this.clazz = clazz;
	}

	public DefaultJob(String pId,String pName,String pDescrption)
	{
		this.id = pId;
		this.name = pName;
		this.descrption = pDescrption;
	}
	
	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getDescription()
	{
		return this.descrption;
	}

	@Override
	public JobExecutor getJobExecutor()
	{
		
		if(ji.isMoreRunningJob()){
			try
            {
	            this.je = this.clazz.newInstance();
            } catch (Exception e)
            {
	            this.je= new AbstractJobExecutor(){

					@Override
                    public void handle() throws Exception
                    {
	                    throw new Exception("在实例化JobExecutor时出错");
	                    
                    }
	            	
	            };
	        }
			this.je.setJob(this);
		}
		
		
		return this.je;
		
	}

	@Override
	public JobInfo getJobInfo()
	{
		return this.ji;
	}

}
