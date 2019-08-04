// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.util.Collections;
import java.util.List;

public class TaskDuration {
	private final List<TaskTimes> times;
	
	public TaskDuration(TaskTimes times) {
		this.times = Collections.singletonList(times);
	}
	
	public TaskDuration(List<TaskTimes> times) {
		this.times = times;
	}

	@Override
	public String toString() {
		long totalTime = times.stream()
				.map(TaskTimes::getDuration)
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
