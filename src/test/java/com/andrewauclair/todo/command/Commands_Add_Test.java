// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


class Commands_Add_Test extends CommandsBaseTestCase {
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
		tasks.addList("one");
		commands.execute(printStream, "add " + option + " one -n \"Test\"");

		assertOutput(
				"Added task 1 - 'Test'",
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
	void add_command_sets_time_charge() {
		commands.execute(printStream, "add --charge \"Issues\" -n \"Test 1\"");
		
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
				"Unknown value '\"Test\"'.",
				""
		);
	}

	@Test
	void add_task_to_a_list_in_a_group() {
		commands.execute(printStream, "create-group /test");

		outputStream.reset();

		tasks.addList("/test/one");

		tasks.setActiveList("/test/one");

		commands.execute(printStream, "add -n \"Test 1\"");

		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}
}
