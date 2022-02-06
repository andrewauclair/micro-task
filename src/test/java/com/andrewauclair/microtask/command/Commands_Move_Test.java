// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void move_command_help() {
		commands.execute(printStream, "move --help");

		assertOutput(
				"Usage:  move [-h] COMMAND",
				"Move a task, list or group.",
				"  -h, --help   Show this help message.",
				"Commands:",
				"  task",
				"  list",
				"  group"
		);
	}
}
