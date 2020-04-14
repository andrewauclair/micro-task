// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group.name;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ExistingTaskGroupName_Equals_Test {
	@Test
	void existing_task_group_name_equals() {
		EqualsVerifier.forClass(ExistingTaskGroupName.class)
				.verify();
	}
}
