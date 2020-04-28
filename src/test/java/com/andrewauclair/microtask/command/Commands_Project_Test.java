// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Project_Test extends CommandsBaseTestCase {
	@Test
	void project_command_help() {
		commands.execute(printStream, "project --help");

		assertOutput(
				"Usage:  project [-h] [--progress] [-n=<name>]",
				"  -h, --help          Show this help message.",
				"  -n, --name=<name>",
				"      --progress"
		);
	}
}
