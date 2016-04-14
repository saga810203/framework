package org.jfw.util.execut.task;

public abstract class FixedDelayTask extends AbstractTask {
	private long delay;
	

	protected FixedDelayTask(){
		this.delay = 1000*60;
		this.nextRunningTime = System.currentTimeMillis()+ this.delay;
	}
	public void setDelay(long delay) {
		if(delay<=0) throw new IllegalArgumentException("FixedDelayTask delay must >0 ");
		this.delay = delay;
	}
	@Override
	protected void updateNextRunningTime() {
		this.nextRunningTime = System.currentTimeMillis() + this.delay;
		
	}
	@Override
	protected void resetNextRunningTime() {
		this.nextRunningTime = System.currentTimeMillis()+ this.delay;		
	}
	

}
