// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Task_hasTaskWithID_Test extends TaskBaseTestCase {
	@Test
	void has_task_with_id_when_task_is_on_finished_list() {
		tasks.addList(newList("/test"), true);
		tasks.addTask("Test", existingList("/test"));

		tasks.finishList(existingList("/test"));

		assertTrue(tasks.hasTaskWithID(1));
	}
}
