// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTimes_toString_Test {
	@Test
	void task_times_to_string_with_start_and_stop_times() {
		assertEquals("1234 - 4567, project='Project', feature='Feature'", new TaskTimes(1234, 4567, "Project", "Feature").toString());
	}

	@Test
	void task_times_to_string_with_only_start_time() {
		assertEquals("1234, project='Project', feature='Feature'", new TaskTimes(1234, "Project", "Feature").toString());
	}

	@Test
	void task_times_description() {
		TaskTimes taskTimes = new TaskTimes(1561078202, 1561079202);
		assertEquals("06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM", taskTimes.description(ZoneId.of("America/Chicago")));
	}
}
