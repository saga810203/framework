package org.jfw.util.scheduler.impl;

public abstract class AbstractLoopJobExecutor extends AbstractJobExecutor
{

	protected abstract void initLoop();
	
	protected abstract boolean hasNextLoop();
	protected abstract boolean doLoop();
	
	
	protected abstract void finalizeLoop();
	
	@Override
	public void handle() throws Exception
	{
		if(this.isCancled) return;
		try{
			this.initLoop();
			while(this.hasNextLoop())
			{
				if(this.isCancled) return;
				this.doLoop();
			}			
		}catch(Exception e)
		{
			finalizeLoop();
			throw e;
		}
	}
}
