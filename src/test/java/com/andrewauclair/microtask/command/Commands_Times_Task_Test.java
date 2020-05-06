// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Locale;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

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

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "   16m 40s         1  Test 1     " + ANSI_RESET,
				"   16m 40s         2  Test 2     ",
				ANSI_BG_GRAY + "   16m 40s         3  Test 3     " + ANSI_RESET,
				"   16m 40s         4  Test 4     ",
				ANSI_BG_GRAY + "   16m 40s         5  Test 5     " + ANSI_RESET,
				"   16m 40s         6  Test 6     ",
				ANSI_BG_GRAY + "   16m 40s         7  Test 7     " + ANSI_RESET,
				"   16m 40s         8  Test 8     ",
				"",
				"2h 13m 20s   Total",
				""
		);
	}
	
	@Test
	void times_cuts_off_title_when_its_longer_than_width_of_terminal() {
		addTaskWithTimes("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times", 1561078202, 1561078202 + 1000);
		addTaskWithTimes("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times", 1561078202, 1561078202 + 1000);
		addTaskWithTimes("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times", 1561078202, 1561078202 + 1000);
		addTaskWithTimes("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times", 1561078202, 1561078202 + 1000);
		addTaskWithTimes("Test 5", 1561478202, 1561478202 + 1000);
		addTaskWithTimes("Test 6", 1561578202, 1561578202 + 1000);
		addTaskWithTimes("Test 7", 1561678202, 1561678202 + 1000);
		addTaskWithTimes("Test 8", 1561778202, 1561778202 + 1000);

		tasks.startTask(existingID(1), false);
		tasks.startTask(existingID(2), true);
		tasks.setRecurring(existingID(3), true);
		
		Mockito.when(osInterface.getTerminalWidth()).thenReturn(80);
		
		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --all-time");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r + "               ",
				ANSI_BG_GREEN + "1h 23m 20s  *      2  Very long titles will be cut off at the side of the scr..." + ANSI_RESET,
				"   33m 20s    F    1  Very long titles will be cut off at the side of the scr...",
				ANSI_BG_GRAY + "   16m 40s         4  Very long titles will be cut off at the side of the scr..." + ANSI_RESET,
				"   16m 40s         5  Test 5                                                    ",
				ANSI_BG_GRAY + "   16m 40s         6  Test 6                                                    " + ANSI_RESET,
				"   16m 40s         7  Test 7                                                    ",
				ANSI_BG_GRAY + "   16m 40s         8  Test 8                                                    " + ANSI_RESET,
				"   16m 40s   R     3  Very long titles will be cut off at the side of the scr...",
				"",
				"3h 36m 40s   Total",
				""
		);
	}

	@Test
	void times_all_time_prints_all_time_numbers__total_only() {
		addTaskWithTimes("Test 1", 1561078202, 1561078202 + 1000);
		addTaskWithTimes("Test 2", 1561178202, 1561178202 + 1000);
		addTaskWithTimes("Test 3", 1561278202, 1561278202 + 1000);
		addTaskWithTimes("Test 4", 1561378202, 1561378202 + 1000);
		addTaskWithTimes("Test 5", 1561478202, 1561478202 + 1000);
		addTaskWithTimes("Test 6", 1561578202, 1561578202 + 1000);
		addTaskWithTimes("Test 7", 1561678202, 1561678202 + 1000);
		addTaskWithTimes("Test 8", 1561778202, 1561778202 + 1000);

		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --total --all-time");

		assertOutput(
				"Total times",
				"",
				"2h 13m 20s   Total",
				""
		);
	}
}
