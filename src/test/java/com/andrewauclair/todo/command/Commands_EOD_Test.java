// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class Commands_EOD_Test extends CommandsBaseTestCase {
	@BeforeEach
	void setup() throws IOException {
		super.setup();

		osInterface.setIncrementTime(false);

		// start at 10:19 - 1:19, 2:19 - 4:19
		addTaskWithTimes("Test 1", 1565965147, 1565975947); // 3 hours
		addTaskWithTimes("Test 2", 1565979547, 1565986747); // 2 hours, 1 hour after the stop of first task

		// end of day is at 7:19 for an 8 hour day
	}

	@Test
	void eight_hour_day() {
		commands.execute(printStream, "eod -h 8");

		assertOutput(
				"End of Day is 06:19:07 PM",
				""
		);
	}

	@Test
	void nine_hour_day() {
		commands.execute(printStream, "eod --hours 9");

		assertOutput(
				"End of Day is 07:19:07 PM",
				""
		);
	}

	@Test
	void hours_parameter_is_required() {
		commands.execute(printStream, "eod");

		assertOutput(
				"Missing hours argument.",
				""
		);
	}
}