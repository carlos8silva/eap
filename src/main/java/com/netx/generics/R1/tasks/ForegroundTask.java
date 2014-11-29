package com.netx.generics.R1.tasks;


public abstract class ForegroundTask extends Task {

	public final void run() {
		try {
			performWork();
		}
		catch(Throwable t) {
			getLogger().error(t);
		}
	    // finish this task's work:
		// this needs must be called even if there is an exception thrown,
		// so the task does not get blocked.
		finishWork();
	}
	
	protected abstract void performWork() throws Throwable;
}
