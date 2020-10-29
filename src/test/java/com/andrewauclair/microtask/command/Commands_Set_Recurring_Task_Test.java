// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Set_Recurring_Task_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_recurring_true_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set task 1 --recurring");

		Task task = tasks.getTask(existingID(1));

		assertTrue(task.recurring);

		assertOutput(
				"Set recurring for task 1 - 'Test 1' to true",
				""
		);
	}

	@Test
	void execute_set_recurring_false_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set task 1 --not-recurring");

		Task task = tasks.getTask(existingID(1));

		assertFalse(task.recurring);

		assertOutput(
				"Set recurring for task 1 - 'Test 1' to false",
				""
		);
	}
}
