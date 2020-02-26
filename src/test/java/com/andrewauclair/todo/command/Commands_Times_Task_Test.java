// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import java.util.Locale;

// Test for a simple times command to execute out a task times list, might just be a temporary step towards bigger better features
class Commands_Times_Task_Test extends CommandsBaseTestCase {
	@Test
	void times_with_too_many_arguments_prints_invalid_command() {
		commands.execute(printStream, "times 1 2");

		assertOutput(
				"Unmatched arguments from index 1: '1', '2'",
				""
		);
	}

	@Test
	void times_with_no_options_is_invalid() {
		commands.execute(printStream, "times");

		assertOutput(
				"Invalid command.",
				""
		);
	}

	@Test
	void times_all_time_prints_all_time_numbers() {
		addTaskWithTimes("Test 1", 1561078202, 1561078202 + 1000);
		addTaskWithTimes("Test 2", 1561178202, 1561178202 + 1000);
		addTaskWithTimes("Test 3", 1561278202, 1561278202 + 1000);
		addTaskWithTimes("Test 4", 1561378202, 1561378202 + 1000);
		addTaskWithTimes("Test 5", 1561478202, 1561478202 + 1000);
		addTaskWithTimes("Test 6", 1561578202, 1561578202 + 1000);
		addTaskWithTimes("Test 7", 1561678202, 1561678202 + 1000);
		addTaskWithTimes("Test 8", 1561778202, 1561778202 + 1000);

		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --all-time");

		assertOutput(
				"Times",
				"",
				"   16m 40s   1 - 'Test 1'",
				"   16m 40s   2 - 'Test 2'",
				"   16m 40s   3 - 'Test 3'",
				"   16m 40s   4 - 'Test 4'",
				"   16m 40s   5 - 'Test 5'",
				"   16m 40s   6 - 'Test 6'",
				"   16m 40s   7 - 'Test 7'",
				"   16m 40s   8 - 'Test 8'",
				"",
				"2h 13m 20s   Total",
				""
		);
	}
}
