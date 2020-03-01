// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;

import java.util.Collections;
import java.util.List;

public final class TaskDuration {
	private final List<TaskTimes> times;
	private final OSInterface osInterface;

	public TaskDuration(TaskTimes times, OSInterface osInterface) {
		this.times = Collections.singletonList(times);
		this.osInterface = osInterface;
	}

	public TaskDuration(Task task, OSInterface osInterface) {
		times = task.getStartStopTimes();
		this.osInterface = osInterface;
	}

	@Override
	public String toString() {
		long totalTime = times.stream()
				.map(times -> times.getDuration(osInterface))
				.reduce(0L, Long::sum);

		return Utils.formatTime(totalTime, Utils.HighestTime.None);
	}
}
