// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.project.name;

import com.andrewauclair.microtask.project.NewProject;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class NewProject_Equals_Test {
	@Test
	void new_project_name_equals() {
		EqualsVerifier.forClass(NewProject.class)
				.verify();
	}
}
