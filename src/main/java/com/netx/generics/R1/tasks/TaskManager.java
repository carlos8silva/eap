package com.netx.generics.R1.tasks;
import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.basic.R1.logging.Logger;


public class TaskManager {

	private final Queue<BackgroundTask> _background;
	private final Queue<ForegroundTask> _foreground;
	private final Queue<BackgroundTask> _suspended;
	private final Logger _logger;
	private int _maxConcurrentTasks;
	private boolean _stopped;

	public TaskManager(Logger logger, int maxConcurrentTasks) {
		Checker.checkNull(logger, "logger");
		Checker.checkIllegalValue(maxConcurrentTasks, 0, "maxConcurrentTasks");
		_background = new LinkedList<BackgroundTask>();
		_foreground = new LinkedList<ForegroundTask>();
		_suspended  = new LinkedList<BackgroundTask>();
		_logger = logger;
		_maxConcurrentTasks = maxConcurrentTasks;
		_stopped = false;
	}

	public TaskManager(int maxConcurrentTasks) {
		this(Globals.getLogger(), maxConcurrentTasks);
	}

	public int getMaxConcurrentTasks() {
		_checkStopped();
		return _maxConcurrentTasks;
	}

	public void setMaxConcurrentTasks(int numThreads) {
		Checker.checkIllegalValue(numThreads, 0, "numThreads");
		_checkStopped();
		_maxConcurrentTasks = numThreads;
	}

	public Logger getLogger() {
		return _logger;
	}
	
	public int getNumRunningForegroundTasks() {
		_checkStopped();
		return _foreground.size();
	}

	public int getNumRunningBackgroundTasks() {
		_checkStopped();
		return _background.size();
	}

	public int getNumRunningTasks() {
		return getNumRunningForegroundTasks() + getNumRunningBackgroundTasks();
	}

	public int getNumSuspendedTasks() {
		_checkStopped();
		return _suspended.size();
	}

	public boolean startTask(Task task) {
		Checker.checkNull(task, "task");
		_checkStopped();
		task.init(this);
		if(task instanceof ForegroundTask) {
			if(getNumRunningTasks() == _maxConcurrentTasks) {
				if(_background.size() == 0) {
					// no threads can be suspended:
					return false;
				}
				else {
					// suspend a background thread:
					BackgroundTask back = _background.remove();
					back.suspend();
					_suspended.add(back);
				}
			}
			// task must be enqueued before started:
			_foreground.add((ForegroundTask)task);
			task.start();
			return true;
		}
		else if(task instanceof BackgroundTask) {
			if(getNumRunningTasks() == _maxConcurrentTasks) {
				return false;
			}
			else {
				// task must be enqueued before started:
				_background.add((BackgroundTask)task);
				task.start();
				return true;
			}
		}
		else {
			throw new IntegrityException(task.getClass().getName());
		}
	}

	public void stop() {
		_checkStopped();
		_logger.info(this+" is being stopped");
		Iterator<?> itForeground = _foreground.iterator();
		while(itForeground.hasNext()) {
			Task t = (Task)itForeground.next();
			t.getThread().interrupt();
		}
		Iterator<?> itBackground = _background.iterator();
		while(itBackground.hasNext()) {
			BackgroundTask t = (BackgroundTask)itBackground.next();
			t.stop();
		}
		_stopped = true;
	}

	public boolean isStopped() {
		return _stopped;
	}
	
	// for Task:
	void finish(Task t) {
		if(t instanceof ForegroundTask) {
			if(!_foreground.remove(t)) {
				throw new IntegrityException();
			}
		}
		else if(t instanceof BackgroundTask) {
			if(!_background.remove(t)) {
				throw new IntegrityException();
			}
		}
		else {
			throw new IntegrityException(t.getClass().getName());
		}
		// resume suspended tasks:
		if(_suspended.size() > 0) {
			BackgroundTask back = _suspended.remove();
			back.resume();
			_background.add(back);
		}
	}

	private void _checkStopped() {
		//TODO change after and test
		//if(_stopped) {
		//	throw new IllegalStateException("this TaskManager has been stopped");
		//}
	}
}
