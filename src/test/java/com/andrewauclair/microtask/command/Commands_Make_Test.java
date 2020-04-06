// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Make_Test extends CommandsBaseTestCase {
	@Test
	void make_command_help() {
		commands.execute(printStream, "mk --help");

		assertOutput(
				"Usage:  mk (-l=<list> | -g=<group>) [-h]",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>"
		);
	}
}