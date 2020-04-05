// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Commands_Next_Test extends CommandsBaseTestCase {
	@Test
	void execute_next_command_for_5_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5");

		assertOutput(
				"Next 5 Tasks To Complete",
				"",
				"1 - 'Test 1'",
				"2 - 'Test 2'",
				"3 - 'Test 3'",
				"4 - 'Test 4'",
				"5 - 'Test 5'",
				""
		);
	}

	@Test
	void execute_next_command_for_2_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		commands.execute(printStream, "next --count 2");

		assertOutput(
				"Next 2 Tasks To Complete",
				"",
				"1 - 'Test 1'",
				"2 - 'Test 2'",
				""
		);
	}

	@Test
	void next_command_skips_finished_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		tasks.finishTask(2);
		tasks.startTask(1, false);

		commands.execute(printStream, "next -c 2");

		assertOutput(
				"Next 2 Tasks To Complete",
				"",
				"1 - 'Test 1'",
				"3 - 'Test 3'",
				""
		);
	}

	@Test
	void next_command_skips_recurring_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		tasks.setRecurring(2, true);

		commands.execute(printStream, "next -c 2");

		assertOutput(
				"Next 2 Tasks To Complete",
				"",
				"1 - 'Test 1'",
				"3 - 'Test 3'",
				""
		);
	}

	@Test
	void next_command_prints_all_available_if_less_than_required() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		commands.execute(printStream, "next -c 5");

		assertOutput(
				"Next 2 Tasks To Complete",
				"",
				"1 - 'Test 1'",
				"2 - 'Test 2'",
				""
		);
	}

	@Test
	void next_command_help() {
		commands.execute(printStream, "next --help");

		assertOutput(
				"Usage:  next [-h] [-c=<count>]",
				"  -c, --count=<count>",
				"  -h, --help            Show this help message."
		);
	}
}
