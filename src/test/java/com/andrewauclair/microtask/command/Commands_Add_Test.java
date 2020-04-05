// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Add_Test extends CommandsBaseTestCase {
	@Test
	void execute_add_command() {
		commands.execute(printStream, "add --name \"Task 1\"");

		assertOutput(
				"Added task 1 - 'Task 1'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);
	}

	@Test
	void add_recurring_task() {
		commands.execute(printStream, "add --recurring -n \"Test\"");

		assertOutput(
				"Added task 1 - 'Test'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), true)
		);
	}

	@Test
	void add_task_to_specific_list() {
		tasks.addList("one", true);
		commands.execute(printStream, "add --list one -n \"Test\"");

		assertOutput(
				"Added task 1 - 'Test'",
				"to list '/one'",
				""
		);

		assertThat(tasks.getTasksForList("one")).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);
	}

	@Test
	void add_task_to_a_list_in_a_group() {
		tasks.addList("/test/one", true);

		tasks.setActiveList("/test/one");

		commands.execute(printStream, "add -n \"Test 1\"");

		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}

	@Test
	void start_task_when_adding_it() {
		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "add -s -n \"Test\"");

		assertEquals(TaskState.Active, tasks.getTask(1).state);

		assertOutput(
				"Added task 1 - 'Test'",
				"",
				"Started task 1 - 'Test'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);
	}

	@Test
	void add_command_help() {
		commands.execute(printStream, "add --help");

		assertOutput(
				"Usage:  add [-hrs] [-l=<list>] -n=<name>",
				"  -h, --help          Show this help message.",
				"  -l, --list=<list>",
				"  -n, --name=<name>",
				"  -r, --recurring",
				"  -s, --start"
		);
	}
}
