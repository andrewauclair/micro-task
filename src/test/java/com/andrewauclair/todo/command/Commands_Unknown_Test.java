// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

class Commands_Unknown_Test extends CommandsBaseTestCase {
	@Test
	void prints_unknown_command_when_command_is_not_found() {
		commands.execute(printStream, "junk");

		assertOutput(
				"Unknown command.",
				""
		);
	}
}
