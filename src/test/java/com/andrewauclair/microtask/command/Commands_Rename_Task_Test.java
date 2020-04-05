// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Rename_Task_Test extends CommandsBaseTestCase {
	@Test
	void renaming_a_task() {
		tasks.addTask("Testing the rename of a task");

		commands.execute(printStream, "rename --task 1 -n \"This is the new name of the task\"");

		assertOutput(
				"Renamed task 1 - 'This is the new name of the task'",
				""
		);
	}

	@Test
	void renaming_a_task_ignores_extra_whitespace() {
		tasks.addTask("Testing the rename of a task");

		commands.execute(printStream, "rename -t 1 --name \"This is the new name of the task\"      ");

		assertOutput(
				"Renamed task 1 - 'This is the new name of the task'",
				""
		);
	}
}
