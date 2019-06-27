// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskDurationTest {
	// TODO Only show hours and minutes if they're not 0
	@Test
	void duration_for_single_task_time_is_difference_in_times() {
		TaskTimes taskTimes = new TaskTimes(1561078202, 1561079202);

		TaskDuration duration = new TaskDuration(taskTimes);

		assertEquals("00h 16m 40s", duration.toString());
	}

	@Test
	void duration_for_multiple_tasks_is_total_duration() {
		List<TaskTimes> times = Arrays.asList(
				new TaskTimes(1561078202, 1561079202),
				new TaskTimes(1561080202, 1561081202),
				new TaskTimes(1561082202, 1561083202)
		);

		TaskDuration duration = new TaskDuration(times);

		assertEquals("00h 50m 00s", duration.toString());
	}
}
