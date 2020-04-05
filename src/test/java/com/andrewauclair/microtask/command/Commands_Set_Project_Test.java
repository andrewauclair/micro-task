// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_Project_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_project_command_for_list() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-list --list /test --project=\"Issues\"");

		assertEquals("Issues", tasks.getProjectForTask(1));

		assertOutput(
				"Set project for list '/test' to 'Issues'",
				""
		);
	}

	@Test
	void execute_set_project_command_for_group() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-group --group /test/ --project \"Issues\"");

		assertEquals("Issues", tasks.getProjectForTask(1));

		assertOutput(
				"Set project for group '/test/' to 'Issues'",
				""
		);
	}
}
