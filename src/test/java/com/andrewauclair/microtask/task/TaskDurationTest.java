// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.MockOSInterface;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskDurationTest {
	private final MockOSInterface osInterface = new MockOSInterface();
	
	@Test
	void duration_for_single_task_time_is_difference_in_times() {
		TaskTimes taskTimes = new TaskTimes(1561078202, 1561079202);
		
		TaskDuration duration = new TaskDuration(taskTimes, osInterface);

		assertEquals("16m 40s", duration.toString());
	}

	@Test
	void duration_for_multiple_tasks_is_total_duration() {
		List<TaskTimes> times = Arrays.asList(
				new TaskTimes(1561078202, 1561079202),
				new TaskTimes(1561080202, 1561081202),
				new TaskTimes(1561082202, 1561083202),
				new TaskTimes(1561082202, 1561083202),
				new TaskTimes(1561083202)
		);
		
		Task task = newTask(1, "Test", TaskState.Inactive, 1000, times);
		
		osInterface.setTime(1561083288);
		
		TaskDuration duration = new TaskDuration(task, osInterface);
		
		assertEquals("1h  8m  6s", duration.toString());
	}

	@Test
	void duration_hides_hours_when_not_needed() {
		TaskTimes taskTimes = new TaskTimes(0, 1000);
		
		TaskDuration duration = new TaskDuration(taskTimes, osInterface);

		assertEquals("16m 40s", duration.toString());
	}

	@Test
	void duration_hides_hours_and_minutes_when_not_needed() {
		TaskTimes taskTimes = new TaskTimes(0, 5);
		
		TaskDuration duration = new TaskDuration(taskTimes, osInterface);

		assertEquals(" 5s", duration.toString());
	}

	@Test
	void duration_shows_minutes_and_seconds_when_zero_when_hours_greater_than_0() {
		TaskTimes taskTimes = new TaskTimes(0, 3600);
		
		TaskDuration duration = new TaskDuration(taskTimes, osInterface);

		assertEquals("1h  0m  0s", duration.toString());
	}
}
