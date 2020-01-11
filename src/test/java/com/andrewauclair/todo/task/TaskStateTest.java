// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskStateTest {
	@Test
	void task_state_inactive() {
		assertEquals(0, TaskState.Inactive.getValue());
		assertEquals("Inactive", TaskState.Inactive.toString());
	}

	@Test
	void task_state_active() {
		assertEquals(1, TaskState.Active.getValue());
		assertEquals("Active", TaskState.Active.toString());
	}

	@Test
	void task_finished_inactive() {
		assertEquals(2, TaskState.Finished.getValue());
		assertEquals("Finished", TaskState.Finished.toString());
	}
}
