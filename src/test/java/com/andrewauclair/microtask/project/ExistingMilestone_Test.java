// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExistingMilestone_Test extends ProjectBaseTestCase {
	@Test
	void throws_exception_when_milestone_does_not_exist() {
		TaskException exception = assertThrows(TaskException.class, () -> new ExistingMilestone(project, "20.9.3"));

		assertEquals("Milestone '20.9.3' does not exist on project 'micro-task'", exception.getMessage());
	}

	@Test
	void existing_milestone_name() {
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		ExistingMilestone milestone = new ExistingMilestone(project, "20.9.3");

		assertEquals("20.9.3", milestone.getName());
		assertEquals("20.9.3", milestone.toString());
	}
}
