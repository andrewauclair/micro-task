// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Stop_Test extends CommandsBaseTestCase {
	@Test
	void help() {
		commands.execute(printStream, "stop");

		assertOutput(
				"Usage:  stop [-h] [COMMAND]",
				"Stop the active task, list, group, project, feature or tags.",
				"  -h, --help   Show this help message.",
				"Commands:",
				"  task",
				"  list",
				"  group",
				"  project",
				"  feature",
				"  milestone",
				"  tags"
		);
	}
}
