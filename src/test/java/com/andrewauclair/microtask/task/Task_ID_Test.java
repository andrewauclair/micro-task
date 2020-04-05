// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Task_ID_Test extends TaskBaseTestCase {
	@Test
	void task_id_starts_at_1() {
		Task test = tasks.addTask("Test");

		assertEquals(1, test.id);
	}
}
