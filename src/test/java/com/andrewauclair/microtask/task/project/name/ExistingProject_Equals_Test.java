// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.project.name;

import com.andrewauclair.microtask.project.ExistingProject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class ExistingProject_Equals_Test {
	@Test
	void existing_project_name_equals() {
		EqualsVerifier.forClass(ExistingProject.class)
				.verify();
	}
}
