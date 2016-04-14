package org.jfw.util.execut;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import org.jfw.util.execut.task.AbstractTask;

public class ScheduleService implements Service {
	private LinkedList<AbstractTask> tasks = new LinkedList<AbstractTask>();

	private Thread thread = null;

	private ReentrantLock commandLock = new ReentrantLock();
	private LinkedList<Command> commands = new LinkedList<Command>();

	public void addCommand(Shell shell, Object param) {
		commandLock.lock();
		try {
			Command cmd = new Command();
			cmd.shell = shell;
			cmd.paramter = param;

		} finally {
			commandLock.unlock();
		}
	}

	private Command getCommand() {
		commandLock.lock();
		try {
			return commands.poll();
		} finally {
			commandLock.unlock();
		}
	}

	private void handleCommands() {
		Command cmd;
		AbstractTask task;
		while ((cmd = this.getCommand()) != null) {
			switch (cmd.shell) {
			case CREATE:
				task = (AbstractTask) cmd.paramter;
				this.tasks.add(task);
				break;
			case DELETE:
				task = (AbstractTask) cmd.paramter;
				this.tasks.remove(task);
				break;
			case SUSPEND:
				task = (AbstractTask) cmd.paramter;
				task.suspend();
				break;
			case RESUME:
				task = (AbstractTask) cmd.paramter;
				task.resume();
			case STOP:
				this.shutdown();
				break;
			// default:
			//
			}
		}

	}

	private void scheduleTask() {
		for (ListIterator<AbstractTask> it = this.tasks.listIterator(); it.hasNext();) {
			AbstractTask task = it.next();
			if (task.isScheduling()) {
				if (task.isIdle()) {
					if (task.getNextRunningTime() >= System.currentTimeMillis()) {
						task.start();
						this.eService.submit(task);
					}
				}
			}
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void startup() throws Exception {
		this.thread = Thread.currentThread();
		try {
			while (this.thread != null) {
				this.handleCommands();
				this.scheduleTask();
			}
		} finally {
			this.thread = null;
		}

	}

	@Override
	public void shutdown() {
		Thread t = this.thread;
		this.thread = null;
		if (t != null)
			t.interrupt();
	}

	private ExecutorService eService;

	public void setExecutorService(ExecutorService service) {
		this.eService = service;
	}

	public enum Shell {
		CREATE, DELETE, SUSPEND, RESUME, STOP
	}

	private static class Command {
		public Shell shell;
		public Object paramter;
	}

}
