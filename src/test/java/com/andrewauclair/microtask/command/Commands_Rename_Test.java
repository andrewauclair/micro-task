// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Rename_Test extends CommandsBaseTestCase {
	@Test
	void rename_command_help() {
		commands.execute(printStream, "rename --help");

		assertOutput(
				"Usage:  rename [-h] COMMAND",
				"Rename a task, list or group.",
				"  -h, --help   Show this help message.",
				"Commands:",
				"  task",
				"  list",
				"  group"
		);
	}
}
