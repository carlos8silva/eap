package com.netx.generics.R1.tasks;
import com.netx.basic.R1.logging.Logger;


public abstract class Task implements Runnable {

	private final Thread _thread;
	private TaskManager _manager;
	
	protected Task() {
		_thread = new Thread(this);
		_manager = null;
	}
	
	public TaskManager getTaskManager() {
		if(_manager == null) {
			throw new IllegalStateException("task hasn't been started yet");
		}
		else {
			return _manager;
		}
	}

	public Logger getLogger() {
		return getTaskManager().getLogger();
	}

	// For TaskManager:
	void init(TaskManager manager) {
		_manager = manager;
	}

	// For TaskManager:
	Thread getThread() {
		return _thread;
	}

	// For TaskManager:
	void start() {
		_thread.start();
	}

	// Module protected (for Background/ForegroundTask):
	void finishWork() {
		_manager.finish(this);
	}
}
