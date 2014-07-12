package com.netx.generics.R1.tasks;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.eh.IntegrityException;


public abstract class BackgroundTask extends Task {

	private volatile boolean _stopped;
	private volatile boolean _suspended;
	private final List<TaskWorker> _workers;
	private boolean _cleared;

	protected BackgroundTask() {
		_stopped = _suspended = false;
		_workers = new ArrayList<TaskWorker>();
		_cleared = false;
	}
	
	public BackgroundTask addWorker(TaskWorker worker) {
		Checker.checkNull(worker, "worker");
		worker.setTask(this);
		_workers.add(worker);
		return this;
	}

	public BackgroundTask removeWorkers() {
		_cleared = true;
		_workers.clear();
		return this;
	}

	public final void run() {
		try {
			Iterator<?> itWorkers = _workers.iterator();
			if(itWorkers.hasNext()) {
				TaskWorker current = (TaskWorker)itWorkers.next();
			    while(!_stopped) {
			    	current.performWork();
			    	// Get next work block:
			    	if(current.isFinished()) {
			    		// Check whether the task has been cleared:
			    		if(_cleared) {
			    			_cleared = false;
			    			itWorkers = _workers.iterator();
			    		}
			    		// get next worker:
			    		if(itWorkers.hasNext()) {
			    			current = (TaskWorker)itWorkers.next();
			    		}
			    		else {
			    			break;
			    		}
			    	}
			    	// Check whether task has been suspended:
			        if(_suspended) {
		                synchronized(this) {
		                    while(_suspended && !_stopped) {
		            	    	try {
		                            wait();
		            	    	}
		            			catch(InterruptedException ie) {
		            				// background tasks should not be interrupted:
		            				throw new IntegrityException(ie);
		            			}
		                    }
		                }
			        }
			        else {
			        	Thread.yield();
			        }
			    }
			}
		}
		catch(Throwable t) {
			getLogger().error(t);
		}
	    // finish this task's work:
		// this needs must be called even if there is an exception thrown,
		// so the task does not get blocked.
		finishWork();
	}

	// for TaskManager:
	synchronized void stop() {
	    _stopped = true;
	    notify();
	}

	// for TaskManager:
	synchronized void suspend() {
	    _suspended = true;
	}

	// for TaskManager:
	synchronized void resume() {
	    _suspended = false;
	    notify();
	}

}
