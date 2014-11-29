package com.netx.generics.R1.tasks;


public abstract class TaskWorker {

	private boolean _finished;
	private BackgroundTask _task;
	
	protected TaskWorker() {
		_finished = false;
		_task = null;
	}
	
	protected void markFinished() {
		_finished = true;
	}
	
	protected BackgroundTask getTask() {
		return _task;
	}

	protected abstract void performWork() throws Throwable;
	
	//for BackgroundTask:
	boolean isFinished() {
		return _finished;
	}

	//for BackgroundTask:
	void setTask(BackgroundTask task) {
		if(_task != null) {
			throw new IllegalStateException("task has already been assigned");
		}
		_task = task;
	}
}
