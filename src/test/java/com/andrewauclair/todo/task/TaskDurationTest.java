// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskDurationTest {
	@Test
	void duration_for_single_task_time_is_difference_in_times() {
		TaskTimes taskTimes = new TaskTimes(1561078202, 1561079202);

		TaskDuration duration = new TaskDuration(taskTimes);

		assertEquals("16m 40s", duration.toString());
	}

	@Test
	void duration_for_multiple_tasks_is_total_duration() {
		List<TaskTimes> times = Arrays.asList(
				new TaskTimes(1561078202, 1561079202),
				new TaskTimes(1561080202, 1561081202),
				new TaskTimes(1561082202, 1561083202),
				new TaskTimes(1561082202, 1561083202)
		);

		TaskDuration duration = new TaskDuration(times);

		assertEquals("01h 06m 40s", duration.toString());
	}

	@Test
	void duration_hides_hours_when_not_needed() {
		TaskTimes taskTimes = new TaskTimes(0, 1000);

		TaskDuration duration = new TaskDuration(taskTimes);

		assertEquals("16m 40s", duration.toString());
	}

	@Test
	void duration_hides_hours_and_minutes_when_not_needed() {
		TaskTimes taskTimes = new TaskTimes(0, 5);

		TaskDuration duration = new TaskDuration(taskTimes);

		assertEquals("05s", duration.toString());
	}

	@Test
	void duration_shows_minutes_and_seconds_when_zero_when_hours_greater_than_0() {
		TaskTimes taskTimes = new TaskTimes(0, 3600);

		TaskDuration duration = new TaskDuration(taskTimes);

		assertEquals("01h 00m 00s", duration.toString());
	}
}
