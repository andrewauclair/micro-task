// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class Commands_Set_Due_Tasks_Test extends CommandsBaseTestCase {
	@Test
	void set_due_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");

		tasks.setDueDate(existingID(1), 0);
		tasks.setDueDate(existingID(3), 0);

		commands.execute(printStream, "set due-tasks --due 2w");

		Mockito.verify(osInterface).gitCommit("Set due date for task 1 - 'Test 1' to 01/14/1970");
		Mockito.verify(osInterface).gitCommit("Set due date for task 3 - 'Test 3' to 01/14/1970");

		assertEquals(1214600, tasks.getTask(existingID(1)).dueTime);
		assertEquals(1214600, tasks.getTask(existingID(3)).dueTime);

		assertOutput(
				"Set due date for task 1 - 'Test 1' to 01/14/1970",
				"Set due date for task 3 - 'Test 3' to 01/14/1970",
				""
		);
	}

	@Test
	void set_due_tasks_interactively() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");

		tasks.setDueDate(existingID(1), 0);
		tasks.setDueDate(existingID(3), 0);

		Mockito.when(osInterface.promptChoice("set task 1 due in 2w")).thenReturn(true);
		Mockito.when(osInterface.promptChoice("set task 3 due in 2w")).thenReturn(false);

		commands.execute(printStream, "set due-tasks --due 2w --interactive");

		Mockito.verify(osInterface, times(1)).gitCommit("Set due date for task 1 - 'Test 1' to 01/14/1970");

		assertEquals(1214600, tasks.getTask(existingID(1)).dueTime);
		assertEquals(0, tasks.getTask(existingID(3)).dueTime);

		assertOutput(
				"1 - 'Test 1'",
				"Set due date for task 1 - 'Test 1' to 01/14/1970",
				"3 - 'Test 3'",
				""
		);
	}

	@Test
	void set_due_tasks_interactively__different_due_dates() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");

		tasks.setDueDate(existingID(1), 0);
		tasks.setDueDate(existingID(3), 0);
		tasks.setDueDate(existingID(4), 0);

		Mockito.when(osInterface.promptChoice("change due time for task 1")).thenReturn(true);
		Mockito.when(osInterface.promptForString("task 1, due in: ")).thenReturn("2w");
		Mockito.when(osInterface.promptChoice("change due time for task 3")).thenReturn(true);
		Mockito.when(osInterface.promptForString("task 3, due in: ")).thenReturn("1w");
		Mockito.when(osInterface.promptChoice("change due time for task 4")).thenReturn(false);

		commands.execute(printStream, "set due-tasks --interactive");

		Mockito.verify(osInterface, times(1)).gitCommit("Set due date for task 1 - 'Test 1' to 01/14/1970");
		Mockito.verify(osInterface, times(1)).gitCommit("Set due date for task 3 - 'Test 3' to 01/07/1970");

		assertEquals(1214600, tasks.getTask(existingID(1)).dueTime);
		assertEquals(609800, tasks.getTask(existingID(3)).dueTime);

		assertOutput(
				"1 - 'Test 1'",
				"Set due date for task 1 - 'Test 1' to 01/14/1970",
				"3 - 'Test 3'",
				"Set due date for task 3 - 'Test 3' to 01/07/1970",
				"4 - 'Test 4'",
				""
		);
	}
}
