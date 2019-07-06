// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

class Commands_Active_Test extends CommandsBaseTestCase {
	@Test
	void execute_active_command() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1);
		setTime(1561079202);
		commands.execute("active");

		assertOutput(
				"Active task is 1 - 'Task 1'",
				"",
				"Active task is on the 'default' list",
				"",
				"Current time elapsed: 00h 16m 40s",
				""
		);
	}

	@Test
	void prints_no_active_task_when_there_is_no_active_task() {
		commands.execute("active");

		assertOutput(
				"No active task.",
				""
		);
	}
}
