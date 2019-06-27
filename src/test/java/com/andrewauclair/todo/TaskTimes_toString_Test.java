// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTimes_toString_Test {
	@Test
	void task_times_to_string_with_start_and_stop_times() {
		assertEquals("1234 - 4567", new TaskTimes(1234, 4567).toString());
	}

	@Test
	void task_times_to_string_with_only_start_time() {
		assertEquals("1234", new TaskTimes(1234).toString());
	}
}
