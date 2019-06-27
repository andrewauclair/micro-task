// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Active_Test extends CommandsBaseTestCase {
	@Test
	void execute_active_command() {
		tasks.addTask("Task 1");
		tasks.startTask(1);
		commands.execute("active");

		assertEquals("Active task is 1 - \"Task 1\"" + System.lineSeparator(), outputStream.toString());
	}
}
