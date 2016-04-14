package org.jfw.util.execut.task;

public abstract class AbstractTask implements Task, Runnable {
	private volatile long firstRunningTime = -1;
	private volatile long num4Running = 0;
	private volatile long num4Failrue = 0;
	private volatile long lastBeginTime = -1;
	private volatile long lastEndTime = -1;
	protected volatile long nextRunningTime = System.currentTimeMillis();

	private volatile long lastFailrueTime = -1;
	private volatile Throwable lastThrowable = null;
	

	private Thread thread = null;
	private boolean started = false;
	private boolean actived = false;

	@Override
	public boolean isScheduling() {
		return this.actived;
	}

	public void suspend() {
		this.actived = false;
	}

	public void resume() {
		this.actived = true;
		this.resetNextRunningTime();
	}

	@Override
	public boolean isRunning() {
		return this.started;
	}

	public boolean isIdle() {
		return (!this.started) && (this.thread == null);
	}

	public void start() {
		this.started = true;
	}

	@Override
	public long getFirstRunningTime() {
		return this.firstRunningTime;
	}

	@Override
	public long getNumForRunning() {
		return this.num4Running;
	}

	@Override
	public long getNumForFailrue() {
		return this.num4Failrue;
	}

	@Override
	public long getLastBeginTime() {
		return this.lastBeginTime;
	}

	@Override
	public long getLastEndTime() {
		return this.lastEndTime;
	}

	@Override
	public long getLastFailrueTime() {
		return this.lastFailrueTime;
	}

	@Override
	public Throwable getLastThrowable() {
		return this.lastThrowable;
	}
	@Override
	public long getNextRunningTime() {
		return this.nextRunningTime;
	};

	@Override
	public void interrupt() {
		this.started = false;
		this.interruptInternal();
		Thread t = this.thread;
		this.thread = null;
		if (t != null) {
			t.interrupt();
		}
	}
	protected abstract void runInternal() throws Throwable;

	@Override
	public void run() {
		this.thread = Thread.currentThread();
		try {
			if (this.started) {
				try {
					this.lastBeginTime = System.currentTimeMillis();
					if(this.firstRunningTime<0) this.firstRunningTime = this.lastBeginTime;
					++this.num4Running;
					try{					
					this.runInternal();
					}catch(Throwable e){
						this.lastFailrueTime = System.currentTimeMillis();
						this.lastThrowable = e;
						++this.num4Failrue;
					}
					this.lastEndTime = System.currentTimeMillis();
				} finally {
					this.started = false;
				}
			}
		} finally {
			this.updateNextRunningTime();
			this.thread = null;
		}

	}

	protected abstract void interruptInternal();
	protected abstract void updateNextRunningTime();
	protected abstract void resetNextRunningTime();

}
