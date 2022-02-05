// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group.name;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ExistingGroupName_Equals_Test {
	@Test
	void existing_task_group_name_equals() {
		EqualsVerifier.forClass(ExistingGroupName.class)
				.verify();
	}
}
