package org.jfw.util.execut.task;

public abstract class FixedRateTask extends AbstractTask {
	private long rate;
	protected FixedRateTask(){
		this.rate = 1000*60;
		this.nextRunningTime = System.currentTimeMillis();
	}
	public void setDelay(long rate) {
		if (rate <= 0)
			throw new IllegalArgumentException("FixedRateTask rate must >0 ");
		this.rate = rate;
	}

	@Override
	protected void updateNextRunningTime() {
		this.nextRunningTime = this.getLastBeginTime() + this.rate;
	}
	@Override
	protected void resetNextRunningTime() {
		this.nextRunningTime = System.currentTimeMillis();		
	}
	
}
