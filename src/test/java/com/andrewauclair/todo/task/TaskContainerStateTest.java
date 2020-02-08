// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskContainerStateTest {
	@Test
	void task_container_state_inprogress() {
		assertEquals(0, TaskContainerState.InProgress.getValue());
		assertEquals("InProgress", TaskContainerState.InProgress.toString());
	}

	@Test
	void task_container_state_active() {
		assertEquals(1, TaskContainerState.Active.getValue());
		assertEquals("Active", TaskContainerState.Active.toString());
	}

	@Test
	void task_container_finished_inactive() {
		assertEquals(2, TaskContainerState.Finished.getValue());
		assertEquals("Finished", TaskContainerState.Finished.toString());
	}
}
