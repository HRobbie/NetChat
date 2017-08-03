package com.hrobbie.netchat.infra;

public interface TaskObserver {
	/**
	 * on task result
	 * @param task
	 * @param results
	 */
	public void onTaskResult(com.hrobbie.netchat.infra.Task task, Object[] results);

	/**
	 * on task progress
	 * @param task
	 * @param params
	 */
	public void onTaskProgress(com.hrobbie.netchat.infra.Task task, Object[] params);
}
