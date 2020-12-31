// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.task.TaskGroupFinder;
import com.andrewauclair.microtask.task.TaskGroupName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Commands_Add_Project_Test extends CommandsBaseTestCase {
	@Test
	void make_a_new_project_from_an_existing_group() {
		tasks.createGroup(newGroup("/projects/test/"));

		commands.execute(System.out, "add project test");

		assertTrue(projects.hasProject("test"));

		assertOutput(
				"Created project 'test'",
				""
		);
	}

	@Test
	void make_a_new_project_with_a_new_group_name() {
		commands.execute(System.out, "add project test");

		assertTrue(new TaskGroupFinder(tasks).hasGroupPath(new TaskGroupName(tasks, "/projects/test/")));
		assertTrue(projects.hasProject("test"));

		assertOutput(
				"Created project 'test'",
				""
		);
	}

	@Test
	void provides_error_message_when_project_already_exists() {
		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "add project test");

		assertOutput(
				"Invalid value for positional parameter at index 0 (<project>): Project 'test' already exists.",
				""
		);
	}
}
