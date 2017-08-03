package com.hrobbie.netchat.infra;

public class TraceTaskScheduler extends WrapTaskScheduler {
	public TraceTaskScheduler(com.hrobbie.netchat.infra.TaskScheduler wrap) {
		super(wrap);
	}

	@Override
	public void reschedule(com.hrobbie.netchat.infra.Task task) {
		trace("reschedule " + task.dump(true));
		
		super.reschedule(task);
	}
	
	private final void trace(String msg) {

	}
}
