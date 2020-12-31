// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group.name;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class NewTaskGroupName_Equals_Test {
	@Test
	void new_task_group_name_equals() {
		EqualsVerifier.forClass(NewTaskGroupName.class)
				.verify();
	}
}
