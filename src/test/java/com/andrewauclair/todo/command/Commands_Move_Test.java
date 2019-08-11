// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void execute_move_command() {
		tasks.addList("one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "move 1 one");

		assertEquals("/one", tasks.findListForTask(1));

		assertOutput(
				"Moved task 1 to list 'one'",
				""
		);
	}
	
	@Test
	void move_task_between_lists_in_different_groups() {
		tasks.addList("/one/two/three");
		tasks.addList("/one/test/five");
		tasks.setCurrentList("/one/two/three");
		
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "move 1 /one/test/five");
		
		assertEquals("/one/test/five", tasks.findListForTask(1));
		
		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				""
		);
	}
}
