// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.git;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class GitCommand_Equals_Test {
	@Test
	void task_equals_task() {
		EqualsVerifier.forClass(GitCommand.class)
				.verify();
	}
}
