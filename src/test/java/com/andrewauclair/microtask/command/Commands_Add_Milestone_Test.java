// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Commands_Add_Milestone_Test extends CommandsBaseTestCase {
	@Test
	void add_a_milestone() {
		tasks.createGroup(newGroup("/projects/micro-task/"));

		projects.createProject(new NewProject(projects, "micro-task"), true);

		commands.execute(printStream, "add milestone micro-task 20.9.3");

		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));

		assertTrue(project.hasMilestone("20.9.3"));

		assertOutput(
				"Created milestone '20.9.3' for project 'micro-task'",
				""
		);
	}
}
