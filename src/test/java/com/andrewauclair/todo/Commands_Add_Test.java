// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

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
	@ValueSource(strings = {"--name", "-n"})
	void uses_name_argument(String name) {
		commands.execute(printStream, "add " + name + " \"Test\" --issue 12345");
		
		assertOutput(
				"Added task 1 - 'Test'",
				""
		);
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), 12345, "")
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
	
	@ParameterizedTest
	@ValueSource(strings = {"--issue", "-i"})
	void add_command_sets_issue_number(String param) {
		commands.execute(printStream, "add " + param + " 12345 --name \"Test 1\"");
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), 12345, "")
		);
		
		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}
	
	@Test
	void add_command_sets_time_charge() {
		commands.execute(printStream, "add --charge \"Issues\" -n \"Test 1\"");
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), -1, "Issues")
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
				"Missing name argument.",
				""
		);
	}
}
