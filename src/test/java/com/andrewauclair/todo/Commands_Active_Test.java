// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Active_Test extends CommandsBaseTestCase {
	@Test
	void execute_active_command() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1);
		setTime(1561079202);
		commands.execute("active");

		assertEquals("Active task is 1 - \"Task 1\"" + Utils.NL + Utils.NL +
				"Active task is on the \"default\" list" + Utils.NL + Utils.NL +
				"Current time elapsed: 00h 16m 40s" + Utils.NL, outputStream.toString());
	}
}
