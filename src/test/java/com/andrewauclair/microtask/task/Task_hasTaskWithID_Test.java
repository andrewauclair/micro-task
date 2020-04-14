// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Task_hasTaskWithID_Test extends TaskBaseTestCase {
	@Test
	void has_task_with_id_when_task_is_on_finished_list() {
		tasks.addList("/test", true);
		tasks.addTask("Test", "/test");

		tasks.finishList("/test");

		TaskFinder finder = new TaskFinder(tasks);
		assertTrue(finder.hasTaskWithID(1));
	}
}
