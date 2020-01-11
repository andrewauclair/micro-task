// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.util.Collections;
import java.util.List;

public class TaskDuration {
	private final List<TaskTimes> times;
	private final OSInterface osInterface;
	
	public TaskDuration(TaskTimes times, OSInterface osInterface) {
		this.times = Collections.singletonList(times);
		this.osInterface = osInterface;
	}

//	public TaskDuration(List<TaskTimes> times, OSInterface osInterface) {
//		this.times = times;
//		this.osInterface = osInterface;
//	}
	
	public TaskDuration(Task task, OSInterface osInterface) {
		times = task.getStartStopTimes();
		this.osInterface = osInterface;
	}
	
	@Override
	public String toString() {
		long totalTime = times.stream()
				.map(times -> times.getDuration(osInterface))
				.reduce(0L, Long::sum);

		long hours = totalTime / (60 * 60);
		long minutes = (totalTime - (hours * 60 * 60)) / 60;
		long seconds = (totalTime - (hours * 60 * 60) - (minutes * 60));

		if (hours > 0) {
			return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
		}
		if (minutes > 0) {
			return String.format("%02dm %02ds", minutes, seconds);
		}
		return String.format("%02ds", seconds);
	}
}
