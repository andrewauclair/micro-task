// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

class TaskDuration {
	private final TaskTimes taskTimes;

	TaskDuration(TaskTimes taskTimes) {
		this.taskTimes = taskTimes;
	}

	@Override
	public String toString() {
		long totalTime = taskTimes.getDuration();

		long hours = totalTime / (60 * 60);
		long minutes = (totalTime - (hours * 60 * 60)) / 60;
		long seconds = (totalTime - (hours * 60 * 60) - (minutes * 60));

		return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
	}
}
