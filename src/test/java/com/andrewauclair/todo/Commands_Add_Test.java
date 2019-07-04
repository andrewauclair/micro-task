// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Commands_Add_Test extends CommandsBaseTestCase {
	@Test
	void execute_add_command() {
		commands.execute("add 'Task 1'");
		commands.execute("add 'Task 2'");

		assertEquals("Added task 1 - 'Task 1'" + Utils.NL + Utils.NL +
				"Added task 2 - 'Task 2'" + Utils.NL + Utils.NL, outputStream.toString());

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Task 1"),
				new Task(2, "Task 2")
		);
	}
}
