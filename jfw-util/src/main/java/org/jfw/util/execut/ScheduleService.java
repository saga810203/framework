package org.jfw.util.execut;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScheduleService implements Service {

	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	public ScheduledExecutorService getScheduledExecutorService() {
		return scheduledExecutorService;
	}

	public void setScheduledExecutorService(ScheduledExecutorService ses) {
		if (ses != null && ses != this.scheduledExecutorService) {
			this.scheduledExecutorService.shutdown();
			this.scheduledExecutorService = ses;
		}
	}

	public void schedule(Runnable runnable, Trigger trigger) {
		new ReschedulingRunnable(runnable, trigger).schedule();
	}

	public ScheduledFuture<?> schedule(Runnable command, long delay) {
		return this.scheduledExecutorService.schedule(command, delay, TimeUnit.MILLISECONDS);
	}

	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay) {
		return this.scheduledExecutorService.schedule(callable, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period) {
		return this.scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay) {
		return this.scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay,
				TimeUnit.MILLISECONDS);
	}

	<T> Future<T> submit(Callable<T> task) {
		return this.scheduledExecutorService.submit(task);
	}

	<T> Future<T> submit(Runnable task, T result) {
		return this.scheduledExecutorService.submit(task, result);
	}

	Future<?> submit(Runnable task) {
		return this.submit(task);
	}

	<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.invokeAll(tasks);
	}

	<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return this.scheduledExecutorService.invokeAll(tasks, timeout, TimeUnit.MILLISECONDS);
	}

	<T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return this.scheduledExecutorService.invokeAny(tasks);
	}

	<T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return this.scheduledExecutorService.invokeAny(tasks, timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public void startup() throws Exception {

	}

	@Override
	public void shutdown() {
		scheduledExecutorService.shutdown();
	}

	class ReschedulingRunnable implements Runnable {
		private final Object monitor = new Object();
		private final Runnable delegate;
		private final Trigger trigger;

		ReschedulingRunnable(Runnable delegate, Trigger trigger) {
			this.delegate = delegate;
			this.trigger = trigger;
		}

		public void schedule() {
			synchronized (this.monitor) {
				long delay = this.trigger.nextExecutionTime() - System.currentTimeMillis();
				scheduledExecutorService.schedule(this, delay, TimeUnit.MILLISECONDS);
			}
		}

		@Override
		public void run() {
			this.delegate.run();
			this.schedule();
		}
	}
}
