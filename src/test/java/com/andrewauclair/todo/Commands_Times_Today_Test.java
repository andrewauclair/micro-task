// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.ConsoleColors;
import org.junit.jupiter.api.Test;

class Commands_Times_Today_Test extends CommandsBaseTestCase {
	private static final long SECONDS_IN_DAY = 86400;
	
	@Test
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight() {
		long june20_7_50_02_pm = 1561084202;
		long june20_8_06_42_pm = 1561085202;
		
		long june21_7_50_02_pm = june20_7_50_02_pm + SECONDS_IN_DAY;
		long june21_8_06_42_pm = june20_8_06_42_pm + SECONDS_IN_DAY;
		
		addTaskWithTimes("Test 1 - Day 1", june20_7_50_02_pm, june20_8_06_42_pm);
		addTaskWithTimes("Test 2 - Day 2", june21_7_50_02_pm, june21_8_06_42_pm + 10);
		
		addTaskWithTimes("Test 3 - Day 3", june21_7_50_02_pm + SECONDS_IN_DAY, june21_8_06_42_pm + SECONDS_IN_DAY);
		
		tasks.addList("test");
		tasks.setCurrentList("test");
		addTaskWithTimes("Test 4 - Day 2", june21_7_50_02_pm, june21_8_06_42_pm);

		addTaskWithTimes("Test 5 - Day 2", june21_7_50_02_pm, june21_8_06_42_pm);

		setTime(june21_8_06_42_pm);

		tasks.startTask(5, false);

		setTime(june21_8_06_42_pm);

		tasks.finishTask(4);

		setTime(june21_8_06_42_pm);
		osInterface.setIncrementTime(false);

		commands.execute(printStream, "times --tasks --today");

		assertOutput(
				"Times for day 06/21/2019",
				"",
				"    16m 50s   2 - 'Test 2 - Day 2'",
				"    16m 40s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + "5 - 'Test 5 - Day 2'" + ConsoleColors.ANSI_RESET,
				"    16m 40s F 4 - 'Test 4 - Day 2'",
				"",
				"Total time: 50m 10s",
				""
		);
	}

	@Test
	void times_are_sorted_by_the_daily_time_not_total_time() {
		long june20_7_50_02_pm = 1561084202;
		long june20_8_06_42_pm = 1561085202;

		long june21_7_50_02_pm = june20_7_50_02_pm + SECONDS_IN_DAY;
		long june21_8_06_42_pm = june20_8_06_42_pm + SECONDS_IN_DAY;

		osInterface.setTime(june20_7_50_02_pm);

		addTaskWithTimes("Test 1 - Day 1", june20_7_50_02_pm, june20_8_06_42_pm);
		addTaskWithTimes("Test 2 - Day 2", june21_7_50_02_pm, june21_8_06_42_pm + 6000);

		addTaskTimes(1, 1000, 20000);

		addTaskTimes(1, june21_7_50_02_pm, june21_8_06_42_pm + 5000);

		osInterface.setTime(june21_7_50_02_pm);

		commands.execute(printStream, "times --tasks --today");

		assertOutput(
				"Times for day 06/21/2019",
				"",
				"01h 56m 40s   2 - 'Test 2 - Day 2'",
				"01h 40m 00s   1 - 'Test 1 - Day 1'",
				"",
				"Total time: 03h 36m 40s",
				""
		);
	}
	// TODO Test that this output is cut off on the right if task name is too long, "Execute the instructions in ...", cut off at the space that fits
}
