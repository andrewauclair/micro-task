// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_recurring_true_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --recurring true");

		Optional<Task> task = tasks.getTask(1);

		assertTrue(task.isPresent());

		assertTrue(task.get().isRecurring());
	}

	@Test
	void execute_set_recurring_false_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --recurring false");

		Optional<Task> task = tasks.getTask(1);

		assertTrue(task.isPresent());

		assertFalse(task.get().isRecurring());
	}

	@Test
	void execute_set_project_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --project \"Issues\"");
		
		Optional<Task> task = tasks.getTask(1);
		
		assertTrue(task.isPresent());

		assertEquals("Issues", task.get().getProject());
	}

	@Test
	void execute_set_feature_command() {
		tasks.addTask("Test 1");

		commands.execute(printStream, "set 1 --feature \"Feature\"");

		Optional<Task> task = tasks.getTask(1);

		assertTrue(task.isPresent());

		assertEquals("Feature", task.get().getFeature());
	}
}
