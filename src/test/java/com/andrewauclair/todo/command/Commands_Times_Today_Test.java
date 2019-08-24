// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import org.junit.jupiter.api.Test;

class Commands_Times_Today_Test extends Commands_Times_BaseTestCase {
	@Test
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight() {
		setTime(june17_8_am);

		commands.execute(printStream, "times --tasks --today");

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

	@Test
	void times_are_sorted_by_the_daily_time_not_total_time() {
		setTime(june17_8_am);

		commands.execute(printStream, "times --tasks --today");

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

	@Test
	void recurring_tasks_are_indicated_with_an_R() {
		setTime(june17_8_am);

		commands.execute(printStream, "times --tasks --today");

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
	
	@Test
	void today_for_single_list() {
		setTime(june17_8_am);
		
		commands.execute(printStream, "times --tasks --today --list /one/design");
		
		// TODO I would like the first line to be "Times for list '/one/design' on day 06/17/2019
		assertOutput(
				"Times for day 06/17/2019",
				"",
				"01h 49m 15s F 3 - 'Test 3'",
				"    32m 20s R 5 - 'Test 5'",
				"",
				"Total time: 02h 21m 35s",
				""
		);
	}
	
	@Test
	void specific_day_for_single_list() {
		setTime(june17_8_am);
		
		commands.execute(printStream, "times --tasks -d 17 --list /one/design");
		
		// TODO I would like the first line to be "Times for list '/one/design' on day 06/17/2019
		assertOutput(
				"Times for day 06/17/2019",
				"",
				"01h 49m 15s F 3 - 'Test 3'",
				"    32m 20s R 5 - 'Test 5'",
				"",
				"Total time: 02h 21m 35s",
				""
		);
	}
	// TODO Test that this output is cut off on the right if task name is too long, "Execute the instructions in ...", cut off at the space that fits
}
