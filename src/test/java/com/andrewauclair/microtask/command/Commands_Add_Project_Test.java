// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.task.*;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

		assertTrue(new TaskGroupFinder(tasks).hasGroupPath(new ExistingGroupName(tasks, "/projects/test/")));
		assertTrue(projects.hasProject("test"));

		assertOutput(
				"Created project 'test'",
				""
		);
	}

	@Test
	void creating_new_project_creates_general_list_with_planning_task() {
		commands.execute(printStream, "add project test");

		assertTrue(new TaskListFinder(tasks).hasList(new ExistingListName(tasks, "/projects/test/general")));
		TaskList list = tasks.getList(new ExistingListName(tasks, "/projects/test/general"));

		assertThat(list.getTasks()).containsOnly(
				new TaskBuilder(existingID(1))
						.withTask("Planning")
						.withState(TaskState.Inactive)
						.withRecurring(true)
						.withAddTime(1000)
						.withDueTime(605800)
						.build()
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
