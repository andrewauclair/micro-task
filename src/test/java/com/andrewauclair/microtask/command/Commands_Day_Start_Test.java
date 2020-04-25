// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

public class Commands_Day_Start_Test extends CommandsBaseTestCase {
	final long june20_7_50_pm = 1561078202;

	@Test
	void start_of_day_is_start_of_first_task() {
		osInterface.setTime(june20_7_50_pm);

		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.startTask(existingID(1), false);

		tasks.startTask(existingID(2), false);

		commands.execute(System.out, "day --start");

		assertOutput(
				"Day started at 08:23:22 PM",
				""
		);
	}

	@Test
	void correctly_finds_start_of_day_when_task_was_worked_on_day_before() {
		osInterface.setTime(june20_7_50_pm - 86000);

		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.startTask(existingID(1), false);
		tasks.startTask(existingID(2), false);
		tasks.stopTask();
		osInterface.setTime(june20_7_50_pm);

		tasks.startTask(existingID(2), false);
		tasks.startTask(existingID(1), false);

		commands.execute(System.out, "day --start");

		assertOutput(
				"Day started at 07:50:02 PM",
				""
		);
	}

	@Test
	void shows_day_not_started_when_no_task_time_can_be_found() {
		osInterface.setTime(june20_7_50_pm - 86400);

		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.startTask(existingID(1), false);
		tasks.startTask(existingID(2), false);
		tasks.stopTask();
		osInterface.setTime(june20_7_50_pm);

		commands.execute(System.out, "day --start");

		assertOutput(
				"Day not started.",
				""
		);
	}
}
