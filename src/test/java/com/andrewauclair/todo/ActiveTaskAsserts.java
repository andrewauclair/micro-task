// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActiveTaskAsserts {
	static void assertNoActiveTask(Tasks tasks) {
		assertThrowsNoActiveTaskException(tasks::getActiveTask);
	}
	
	static void assertThrowsNoActiveTaskException(Executable function) {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, function, "Expected no active task");
		
		assertEquals("No active task.", runtimeException.getMessage());
	}
}
