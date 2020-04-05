// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.TaskList;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class EqualsVerifierTests {
	@Test
	void task_group_equals() {
		EqualsVerifier.forClass(TaskGroup.class)
				.withPrefabValues(TaskGroup.class, new TaskGroup("one"), new TaskGroup("two"))
				.withIgnoredFields("parent")
				.verify();
	}

	@Test
	void task_list_equals() {
		EqualsVerifier.forClass(TaskList.class)
				.withPrefabValues(TaskGroup.class, new TaskGroup("one"), new TaskGroup("two"))
				.withIgnoredFields("parent")
				.verify();
	}
}
