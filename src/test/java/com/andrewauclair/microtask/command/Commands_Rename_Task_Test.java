// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Rename_Task_Test extends CommandsBaseTestCase {
	@Test
	void renaming_a_task() {
		tasks.addTask("Testing the rename of a task");

		commands.execute(printStream, "rename task 1 -n \"This is the new name of the task\"");

		assertOutput(
				"Renamed task 1 - 'This is the new name of the task'",
				""
		);
	}

	@Test
	void renaming_a_task_ignores_extra_whitespace() {
		tasks.addTask("Testing the rename of a task");

		commands.execute(printStream, "rename task 1 --name \"This is the new name of the task\"      ");

		assertOutput(
				"Renamed task 1 - 'This is the new name of the task'",
				""
		);
	}

	@Test
	void renaming_task_to_the_same_name_results_in_message() {
		tasks.addTask("Testing the rename of a task");

		Mockito.reset(osInterface);

		commands.execute(printStream, "rename task 1 --name \"Testing the rename of a task\"");

		Mockito.verifyNoMoreInteractions(osInterface);

		assertOutput(
				"Task already has that name.",
				""
		);
	}
}
