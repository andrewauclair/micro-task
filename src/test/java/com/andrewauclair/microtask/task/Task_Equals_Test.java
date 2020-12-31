// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class Task_Equals_Test {
	@Test
	void task_equals_task() {
		EqualsVerifier.forClass(Task.class)
				.verify();
	}
}
