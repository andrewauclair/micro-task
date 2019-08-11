// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.TaskList;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class EqualsVerifierTests {
	@Test
	void task_group_equals() {
		EqualsVerifier.forClass(TaskGroup.class)
				.withPrefabValues(TaskGroup.class, new TaskGroup("one"), new TaskGroup("two"))
				.verify();
	}
	
	@Test
	void task_list_equals() {
		EqualsVerifier.forClass(TaskList.class).verify();
	}
}
