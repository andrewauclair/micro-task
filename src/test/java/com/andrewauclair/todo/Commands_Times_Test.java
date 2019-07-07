// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

class Commands_Times_Test extends CommandsBaseTestCase {
	@Test
	void invalid_command_if_no_tasks_or_lists_flag() {
		commands.execute("times --junk");

		assertOutput(
				"Invalid command.",
				""
		);
	}
}
