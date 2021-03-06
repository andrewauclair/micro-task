// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Change_Test extends CommandsBaseTestCase {
	@Test
	void change_command_help() {
		commands.execute(printStream, "ch --help");

		assertOutput(
				"Usage:  ch (-l=<list> | -g=<group> | -p=<project>) [-h]",
				"Change the current list or group.",
				"  -g, --group=<group>       The group to change to.",
				"  -h, --help                Show this help message.",
				"  -l, --list=<list>         The list to change to.",
				"  -p, --project=<project>   The project to change to."
		);
	}
}
