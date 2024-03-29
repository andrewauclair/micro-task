// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

class Commands_Day_End_Test extends CommandsBaseTestCase {
	@BeforeEach
	public void setup() throws IOException {
		super.setup();

		osInterface.setIncrementTime(false);

		// start at 10:19 - 1:19, 2:19 - 4:19
		addTaskWithTimes("Test 1", 1565965147, 1565975947); // 3 hours
		addTaskWithTimes("Test 2", 1565975947, 1565976059);
		addTaskWithTimes("Test 3", 1565979547, 1565986747); // 2 hours, 1 hour after the stop of first task

		Mockito.when(localSettings.hoursInDay()).thenReturn(6);

		// end of day is at 6:19 for an 8 hour day
	}

	@Test
	void eight_hour_day() {
		commands.execute(printStream, "day --end --hours 8");

		assertOutput(
				"End of Day is in 2h 58m  8s at 06:17:15 PM",
				""
		);
	}

	@Test
	void nine_hour_day() {
		commands.execute(printStream, "day --end --hours 9");

		assertOutput(
				"End of Day is in 3h 58m  8s at 07:17:15 PM",
				""
		);
	}

	@Test
	void eod_command_uses_local_settings_when_hours_option_is_missing() {
		commands.execute(printStream, "day --end");

		assertOutput(
				"End of Day is in 58m  8s at 04:17:15 PM",
				""
		);
	}

	@Test
	void eod_command_prints_day_complete_when_past_end_of_day() {
		commands.execute(printStream, "day --end --hours 1");

		assertOutput(
				"Day complete.",
				""
		);
	}

	@Test
	void day_command_help() {
		commands.execute(printStream, "day --help");

		assertOutput(
				"Usage:  day [-h] [--end] [--start] [--hours=<hours>]",
				"Print the start or end of the day.",
				"      --end             Display the end of the day.",
				"  -h, --help            Show this help message.",
				"      --hours=<hours>   Number of hours in the day.",
				"      --start           Display the start of the day."
		);
	}
}
