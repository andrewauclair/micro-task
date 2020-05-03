// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.list.name;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ExistingListName_Equals_Test {
	@Test
	void existing_task_list_name_equals() {
		EqualsVerifier.forClass(ExistingListName.class)
				.verify();
	}
}
