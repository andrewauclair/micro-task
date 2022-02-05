// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskStateTest {
	@Test
	void task_state_inactive() {
		assertEquals("Inactive", TaskState.Inactive.toString());
	}

	@Test
	void task_state_active() {
		assertEquals("Active", TaskState.Active.toString());
	}

	@Test
	void task_finished_inactive() {
		assertEquals("Finished", TaskState.Finished.toString());
	}
}
