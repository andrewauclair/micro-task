// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Commands_Add_Test extends CommandsBaseTestCase {
	private String[] helpText = new String[]{
			"Usage:  add [-hrs] [-l=<list>] -n=<name>",
			"  -h, --help          Show this help message.",
			"  -l, --list=<list>",
			"  -n, --name=<name>",
			"  -r, --recurring",
			"  -s, --start"
	};

	@Test
	void execute_add_command() {
		commands.execute(printStream, "add -n \"Task 1\"");
		commands.execute(printStream, "add --name \"Task 2\"");

		assertOutput(
				"Added task 1 - 'Task 1'",
				"",
				"Added task 2 - 'Task 2'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				new Task(2, "Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(2000)))
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"--recurring", "-r"})
	void add_recurring_task(String recurring) {
		commands.execute(printStream, "add " + recurring + " -n \"Test\"");

		assertOutput(
				"Added task 1 - 'Test'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), true)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"--list", "-l"})
	void add_task_to_specific_list(String option) {
		tasks.addList("one", true);
		commands.execute(printStream, "add " + option + " one -n \"Test\"");

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
	void add_command_ignores_extra_whitespace() {
		commands.execute(printStream, "add --name \"Task 1\"    ");

		assertOutput(
				"Added task 1 - 'Task 1'",
				""
		);
	}

	@Test
	void add_command_sets_time() {
		commands.execute(printStream, "add -n \"Test 1\"");

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), false)
		);

		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}

	@Test
	void add_with_no_name_argument_outputs_missing_argument() {
		commands.execute(printStream, "add \"Test\"");

		assertOutput(
				"Missing required option '--name=<name>'",
				""
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

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void add_command_help(String parameter) {
		commands.execute(printStream, "add " + parameter);

		assertOutput(
				helpText
		);
	}
}
