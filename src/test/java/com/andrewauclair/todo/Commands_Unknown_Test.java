// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Unknown_Test extends CommandsBaseTestCase {
	@Test
	void prints_unknown_command_when_command_is_not_found() {
		commands.execute("junk");

		assertEquals("Unknown command." + Utils.NL, outputStream.toString());
	}
}
