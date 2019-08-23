// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class Commands_Times_Projects_Test extends CommandsBaseTestCase {
	@Test
	@Disabled
	void test() {
		fail();

		assertOutput(
				"Project times",
				"",
				"project			time", // TODO I'd like to bold this line
				"",
				""
		);
	}
}
