// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void execute_move_command() {
		tasks.addList("one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "move 1 one");

		assertEquals("one", tasks.findListForTask(1));

		assertOutput(
				"Moved task 1 to list 'one'",
				""
		);
	}
}
