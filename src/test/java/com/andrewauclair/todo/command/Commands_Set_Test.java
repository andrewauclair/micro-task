// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_issue_command() {
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set 1 --issue 12345");
		
		Optional<Task> task = tasks.getTask(1);
		
		assertTrue(task.isPresent());
		
		assertEquals(12345, task.get().getIssue());
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
