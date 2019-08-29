// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_recurring_true_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --recurring true");
		
		Task task = tasks.getTask(1);
		
		assertTrue(task.isRecurring());
	}

	@Test
	void execute_set_recurring_false_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --recurring false");
		
		Task task = tasks.getTask(1);
		
		assertFalse(task.isRecurring());
	}

	@Test
	void execute_set_project_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --project \"Issues\"");
		
		Task task = tasks.getTask(1);
		
		assertEquals("Issues", task.getProject());
	}

	@Test
	void execute_set_feature_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --feature \"Feature\"");
		
		Task task = tasks.getTask(1);
		
		assertEquals("Feature", task.getFeature());
	}
}
