// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActiveTaskAsserts {
	static void assertNoActiveTask(Tasks tasks) {
		assertThrowsNoActiveTaskException(tasks::getActiveTask);
	}

	static void assertThrowsNoActiveTaskException(Executable function) {
		TaskException taskException = assertThrows(TaskException.class, function, "Expected no active task");
		
		assertEquals("No active task.", taskException.getMessage());
	}
}
