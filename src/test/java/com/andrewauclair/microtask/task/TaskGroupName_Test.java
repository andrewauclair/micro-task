// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskGroupName_Test extends TaskBaseTestCase {
	@Test
	void throws_exception_if_name_is_group_name() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> new TaskGroupName(tasks, "test"){});

		assertEquals("Group name must end in /", runtimeException.getMessage());
	}
}
