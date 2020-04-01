// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Commands_Active_Test extends CommandsBaseTestCase {
	@Test
	void execute_active_command() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1, false);
		setTime(1561079202);
		commands.execute(printStream, "active");

		assertOutput(
				"Active group is '/'",
				"",
				"Active list is '/default'",
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
		tasks.addList("/test/one/two/three", true);
		tasks.setActiveList("/test/one/two/three");
		tasks.switchGroup("/test/one/");
		
		tasks.addTask("Test");
		
		setTime(1561078202);
		tasks.startTask(1, false);
		setTime(1561079202);
		commands.execute(printStream, "active");
		
		assertOutput(
				"Active group is '/test/one/two/'",
				"",
				"Active list is '/test/one/two/three'",
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
				"Active group is '/'",
				"",
				"Active list is '/default'",
				"",
				"No active task.",
				""
		);
	}
	
	@Test
	void commands_prints_exception_to_System_out_only_once() {
		System.setOut(printStream);
		
		commands.execute(System.out, "active");
		
		assertOutput(
				"Active group is '/'",
				"",
				"Active list is '/default'",
				"",
				"No active task.",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void active_command_help(String parameter) {
		commands.execute(printStream, "active " + parameter);

		assertOutput(
				"Usage:  active [-h]",
				"  -h, --help   Show this help message."
		);
	}
}