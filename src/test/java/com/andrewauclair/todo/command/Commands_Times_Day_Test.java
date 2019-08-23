// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Commands_Times_Day_Test extends Commands_Times_BaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-m 6 -d 17 -y 2019", "-m 6 -d 17", "-d 17 -y 2019"})
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight(String parameters) {
		commands.execute(printStream, "times --tasks " + parameters);

		assertOutput(
				"Times for day 06/17/2019",
				"",
				"01h 49m 15s F 3 - 'Test 3'",
				"01h 01m 39s   2 - 'Test 2'",
				"    32m 20s R 5 - 'Test 5'",
				"    10m 21s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ConsoleColors.ANSI_RESET,
				"",
				"Total time: 03h 33m 35s",
				""
		);
	}
	// TODO Test that this output is cut off on the right if task name is too long, "Execute the instructions in ...", cut off at the space that fits
}
