// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


class Commands_Add_Test extends CommandsBaseTestCase {
	@Test
	void execute_add_command() {
		commands.execute(printStream, "add \"Task 1\"");
		commands.execute(printStream, "add \"Task 2\"");

		assertOutput(
				"Added task 1 - 'Task 1'",
				"",
				"Added task 2 - 'Task 2'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Task 1"),
				new Task(2, "Task 2")
		);
	}

	@Test
	void add_command_ignores_extra_whitespace() {
		commands.execute(printStream, "add \"Task 1\"    ");

		assertOutput(
				"Added task 1 - 'Task 1'",
				""
		);
	}
	
	@Test
	void add_command_sets_issue_number() {
		commands.execute(printStream, "add --issue 12345 \"Test 1\"");
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.emptyList(), 12345, "")
		);
		
		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}
	
	@Test
	void add_command_sets_time_charge() {
		commands.execute(printStream, "add --charge \"Issues\" \"Test 1\"");
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.emptyList(), -1, "Issues")
		);
		
		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}
}
