// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

class Commands_Active_Test extends CommandsBaseTestCase {
	@Test
	void execute_active_command() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1, false);
		setTime(1561079202);
		commands.execute(printStream, "active");

		assertOutput(
				"Active task is 1 - 'Task 1'",
				"",
				"Active task is on the '/default' list",
				"",
				"Current time elapsed: 16m 40s",
				""
		);
	}

	@Test
	void prints_no_active_task_when_there_is_no_active_task() {
		commands.execute(printStream, "active");

		assertOutput(
				"No active task.",
				""
		);
	}
	
	@Test
	void prints_issue_number_when_active_task_has_an_associated_issue() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1, false);
		setTime(1561079202);
		
		tasks.setIssue(1, 12345);
		
		commands.execute(printStream, "active");
		
		assertOutput(
				"Active task is 1 - 'Task 1'",
				"",
				"Issue: 12345",
				"",
				"Active task is on the '/default' list",
				"",
				"Current time elapsed: 16m 40s",
				""
		);
	}
}
