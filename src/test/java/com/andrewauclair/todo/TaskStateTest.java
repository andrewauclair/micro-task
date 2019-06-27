// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskStateTest {
	@Test
	void task_state_inactive() {
		assertEquals(0, Task.TaskState.Inactive.getValue());
		assertEquals("Inactive", Task.TaskState.Inactive.toString());
	}

	@Test
	void task_state_active() {
		assertEquals(1, Task.TaskState.Active.getValue());
		assertEquals("Active", Task.TaskState.Active.toString());
	}

	@Test
	void task_finished_inactive() {
		assertEquals(2, Task.TaskState.Finished.getValue());
		assertEquals("Finished", Task.TaskState.Finished.toString());
	}
}
