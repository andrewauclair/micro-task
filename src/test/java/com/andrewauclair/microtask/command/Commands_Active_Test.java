// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class Commands_Active_Test extends CommandsBaseTestCase {
	@Test
	void execute_active_command() {
		addTask("Task 1");

		setTime(0);

		tasks.startTask(existingID(1), false);

		// 16 minutes and 40 seconds later
		setTime(1000);

		commands.execute(printStream, "active");

		assertOutput(
				"Current group is '/'",
				"",
				"Current list is '/default'",
				"",
				"Active task is 1 - 'Task 1'",
				"",
				"Active task is on the '/default' list",
				"",
				"Current time elapsed: 16m 40s",
				""
		);
	}

	@Test
	void print_active_group_and_active_list() {
		tasks.addGroup(newGroup("/test/one/two/"));
		tasks.addList(newList("/test/one/two/three"), true);
		tasks.setCurrentList(existingList("/test/one/two/three"));
		tasks.setCurrentGroup(existingGroup("/test/one/"));

		tasks.addTask("Test");

		setTime(1561078202);
		tasks.startTask(existingID(1), false);
		setTime(1561079202);
		commands.execute(printStream, "active");

		assertOutput(
				"Current group is '/test/one/two/'",
				"",
				"Current list is '/test/one/two/three'",
				"",
				"Active task is 1 - 'Test'",
				"",
				"Active task is on the '/test/one/two/three' list",
				"",
				"Current time elapsed: 16m 40s",
				""
		);
	}

	@Test
	void prints_no_active_task_when_there_is_no_active_task() {
		commands.execute(printStream, "active");

		assertOutput(
				"Current group is '/'",
				"",
				"Current list is '/default'",
				"",
				"No active task.",
				""
		);
	}

	@Test
	void print_active_context_with_no_values_set() {
		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      none",
				"group:     none",
				"project:   none",
				"feature:   none",
				"milestone: none",
				"tags:      none",
				""
		);
	}

	@Test
	void active_context_task() {
		tasks.addTask("Test");
		tasks.startTask(existingID(1), false);

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      1",
				"list:      none",
				"group:     none",
				"project:   none",
				"feature:   none",
				"milestone: none",
				"tags:      none",
				""
		);
	}

	@Test
	void active_context_list() {
		tasks.getActiveContext().setActiveList(existingList("/default"));

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      /default",
				"group:     none",
				"project:   none",
				"feature:   none",
				"milestone: none",
				"tags:      none",
				""
		);
	}

	@Test
	void active_context_group() {
		tasks.createGroup(newGroup("/projects/test/"));

		tasks.getActiveContext().setActiveGroup(existingGroup("/projects/test/"));

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      none",
				"group:     /projects/test/",
				"project:   none",
				"feature:   none",
				"milestone: none",
				"tags:      none",
				""
		);
	}

	@Test
	void active_context_project() {
		projects.createProject(new NewProject(projects, "test"), true);
		tasks.getActiveContext().setActiveProject(new ExistingProject(projects, "test"));

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      none",
				"group:     none",
				"project:   test",
				"feature:   none",
				"milestone: none",
				"tags:      none",
				""
		);
	}

	@Test
	void active_context_feature() {
		projects.createProject(new NewProject(projects, "test"), true);
		Project project = projects.getProject(new ExistingProject(projects, "test"));
		project.addFeature(new NewFeature(project, "feat"), true);

		tasks.getActiveContext().setActiveFeature(new ExistingFeature(project, "feat"));

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      none",
				"group:     none",
				"project:   none",
				"feature:   feat",
				"milestone: none",
				"tags:      none",
				""
		);
	}

	@Test
	void active_context_milestone() {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		tasks.getActiveContext().setActiveMilestone(new ExistingMilestone(project, "20.9.3"));

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      none",
				"group:     none",
				"project:   none",
				"feature:   none",
				"milestone: 20.9.3",
				"tags:      none",
				""
		);
	}
	@Test
	void active_context_tags() {
		tasks.getActiveContext().setActiveTags(Arrays.asList("design", "phase-1"));

		commands.execute(printStream, "active --context");

		assertOutput(
				"Active Context",
				"",
				"task:      none",
				"list:      none",
				"group:     none",
				"project:   none",
				"feature:   none",
				"milestone: none",
				"tags:      design, phase-1",
				""
		);
	}

	@Test
	void active_command_help() {
		commands.execute(printStream, "active --help");

		assertOutput(
				"Usage:  active [-h] [--context]",
				"Display information about the active task, list and group.",
				"      --context   Display the active context.",
				"  -h, --help      Show this help message."
		);
	}
}
