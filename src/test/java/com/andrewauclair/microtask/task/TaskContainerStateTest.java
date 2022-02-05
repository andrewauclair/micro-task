// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskContainerStateTest {
	@Test
	void task_container_state_inprogress() {
		assertEquals("InProgress", TaskContainerState.InProgress.toString());
	}

	@Test
	void task_container_finished_inactive() {
		assertEquals("Finished", TaskContainerState.Finished.toString());
	}
}
