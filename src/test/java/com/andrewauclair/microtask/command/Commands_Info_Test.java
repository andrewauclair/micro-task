// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

class Commands_Info_Test extends CommandsBaseTestCase {
	@Test
	void info_command_prints_data_related_to_a_task() {
		tasks.setProject(tasks.getListByName("/default"), "Project", true);
		tasks.setFeature(tasks.getListByName("/default"), "Feature", true);

		tasks.addTask(new Task(1, "Test", TaskState.Finished, Arrays.asList(
				new TaskTimes(1000), // add
				new TaskTimes(2000, 3000),
				new TaskTimes(6000, 8000),
				new TaskTimes(8000) // finish
		)));

		commands.execute(printStream, "info 1");

		assertOutput(
				"Info for 1 - 'Test'",
				"",
				"12/31/1969 06:16:40 PM -- added",
				"",
				"12/31/1969 06:33:20 PM - 12/31/1969 06:50:00 PM -- 1",
				"12/31/1969 07:40:00 PM - 12/31/1969 08:13:20 PM -- 2",
				"",
				"12/31/1969 08:13:20 PM -- finished",
				"",
				"on list '/default'",
				"",
				"Project 'Project'",
				"Feature 'Feature'",
				""
		);
	}

	@Test
	void inactive_tasks_do_not_print_a_finish_time() {
		tasks.setProject(tasks.getListByName("/default"), "Project", true);
		tasks.setFeature(tasks.getListByName("/default"), "Feature", true);

		tasks.addTask(new Task(1, "Test", TaskState.Inactive, Arrays.asList(
				new TaskTimes(1000), // add
				new TaskTimes(2000, 3000),
				new TaskTimes(6000, 8000)
		)));

		commands.execute(printStream, "info 1");

		assertOutput(
				"Info for 1 - 'Test'",
				"",
				"12/31/1969 06:16:40 PM -- added",
				"",
				"12/31/1969 06:33:20 PM - 12/31/1969 06:50:00 PM -- 1",
				"12/31/1969 07:40:00 PM - 12/31/1969 08:13:20 PM -- 2",
				"",
				"on list '/default'",
				"",
				"Project 'Project'",
				"Feature 'Feature'",
				""
		);
	}

	@Test
	void active_task_displays_no_stop_time() {
		tasks.setProject(tasks.getListByName("/default"), "Project", true);
		tasks.setFeature(tasks.getListByName("/default"), "Feature", true);

		tasks.addTask(new Task(1, "Test", TaskState.Active, Arrays.asList(
				new TaskTimes(1000), // add
				new TaskTimes(2000, 3000),
				new TaskTimes(6000)
		)));

		commands.execute(printStream, "info 1");

		assertOutput(
				"Info for 1 - 'Test'",
				"",
				"12/31/1969 06:16:40 PM -- added",
				"",
				"12/31/1969 06:33:20 PM - 12/31/1969 06:50:00 PM -- 1",
				"12/31/1969 07:40:00 PM - ",
				"",
				"on list '/default'",
				"",
				"Project 'Project'",
				"Feature 'Feature'",
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

		Mockito.verify(osInterface).copyToClipboard("Test Name");
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
