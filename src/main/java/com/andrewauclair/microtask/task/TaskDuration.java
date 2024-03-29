// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;

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
		times = task.startStopTimes;
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
