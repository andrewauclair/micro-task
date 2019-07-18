// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Debug_Test extends CommandsBaseTestCase {
	@Test
	void execute_debug_enable_command() {
		commands.execute(printStream, "debug enable");

		assertTrue(commands.isDebugEnabled());
	}

	@Test
	void execute_debug_disable_command() {
		commands.execute(printStream, "debug enable");
		commands.execute(printStream, "debug disable");
		assertFalse(commands.isDebugEnabled());
	}

	@Test
	void executing_debug_with_any_text_other_than_enable_or_disable_results_in_an_invalid_command_message() {
		commands.execute(printStream, "debug junk");

		assertOutput(
				"Invalid command.",
				""
		);
	}
}
