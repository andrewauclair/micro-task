// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.Task;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class Task_Equals_Test {
	@Test
	void task_equals_task() {
		EqualsVerifier.forClass(Task.class)
				.verify();
	}
}
