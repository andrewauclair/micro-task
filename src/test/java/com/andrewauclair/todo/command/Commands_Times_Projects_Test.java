// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class Commands_Times_Projects_Test extends CommandsBaseTestCase {
	@Test
	@Disabled
	void test() {


		assertOutput(
				"Project times",
				"",
				"project	feature	   time", // TODO I'd like to bold this line
				"",
				"Project 1  Feature 1  01h 10m 20s",
				"Project 2  Feature 2      25m 29s",
				""
		);
	}
}
