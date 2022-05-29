// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewFeature;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;

class Commands_Info_Test extends CommandsBaseTestCase {
	@BeforeEach
	public void setup() throws IOException {
		super.setup();

		tasks.createGroup(newGroup("/projects/micro-task/"));
		tasks.addList(newList("/projects/micro-task/feature-1"), true);

		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addFeature(new NewFeature(project, "feature-1"), true);

		tasks.setCurrentList(existingList("/projects/micro-task/feature-1"));
	}

	@Test
	void info_command_prints_data_related_to_a_task() {
		tasks.addTask(newTask(newID(2), idValidator, "Test", TaskState.Finished, 1000, 8000,
				Arrays.asList(
				new TaskTimes(2000, 3000),
				new TaskTimes(6000, 8000)
		)));

		commands.execute(printStream, "info 2");

		assertOutput(
				"Info for 2 - 'Test'",
				"",
				"12/31/1969 06:16:40 PM -- added",
				"",
				"12/31/1969 06:33:20 PM - 12/31/1969 06:50:00 PM -- 1",
				"12/31/1969 07:40:00 PM - 12/31/1969 08:13:20 PM -- 2",
				"",
				"12/31/1969 08:13:20 PM -- finished",
				"",
				"on list '/projects/micro-task/feature-1'",
				"",
				"Project 'micro-task'",
				"Feature 'feature-1'",
				""
		);
	}

	@Test
	void inactive_tasks_do_not_print_a_finish_time() {
		tasks.addTask(newTask(newID(2), idValidator, "Test", TaskState.Inactive, 1000,
				Arrays.asList(
				new TaskTimes(2000, 3000),
				new TaskTimes(6000, 8000)
		)));

		commands.execute(printStream, "info 2");

		assertOutput(
				"Info for 2 - 'Test'",
				"",
				"12/31/1969 06:16:40 PM -- added",
				"",
				"12/31/1969 06:33:20 PM - 12/31/1969 06:50:00 PM -- 1",
				"12/31/1969 07:40:00 PM - 12/31/1969 08:13:20 PM -- 2",
				"",
				"on list '/projects/micro-task/feature-1'",
				"",
				"Project 'micro-task'",
				"Feature 'feature-1'",
				"",
				"Due: 01/07/1970",
				""
		);
	}

	@Test
	void active_task_displays_no_stop_time() {
		tasks.addTask(newTask(newID(2), idValidator, "Test", TaskState.Active, 1000,
				Arrays.asList(
				new TaskTimes(2000, 3000),
				new TaskTimes(6000)
		)));

		commands.execute(printStream, "info 2");

		assertOutput(
				"Info for 2 - 'Test'",
				"",
				"12/31/1969 06:16:40 PM -- added",
				"",
				"12/31/1969 06:33:20 PM - 12/31/1969 06:50:00 PM -- 1",
				"12/31/1969 07:40:00 PM - ",
				"",
				"on list '/projects/micro-task/feature-1'",
				"",
				"Project 'micro-task'",
				"Feature 'feature-1'",
				"",
				"Due: 01/07/1970",
				""
		);
	}

	@Test
	void copy_task_name_to_clipboard() {
		tasks.addTask("Test Name");

		commands.execute(printStream, "info 1 --copy-name");

		assertOutput(
				"Copied name of task 1 to the clipboard.",
				""
		);

		Mockito.verify(osInterface).copyToClipboard("Planning");
	}

	@Test
	void info_command_help() {
		commands.execute(printStream, "info --help");

		assertOutput(
				"Usage:  info [-h] [--copy-name] <id>",
				"Display info for a task.",
				"      <id>          The task to display information for.",
				"      --copy-name   Copy the name of the task to the clipboard.",
				"  -h, --help        Show this help message."
		);
	}
}
