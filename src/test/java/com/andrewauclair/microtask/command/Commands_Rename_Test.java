// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Rename_Test extends CommandsBaseTestCase {
	@Test
	void rename_command_help() {
		commands.execute(printStream, "rename --help");

		assertOutput(
				"Usage:  rename (-l=<list> | -g=<group> | -t=<id>) [-h] -n=<name>",
				"Rename a task, list or group.",
				"  -g, --group=<group>   Group to rename.",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>     List to rename.",
				"  -n, --name=<name>     The new name for the task, list or group.",
				"  -t, --task=<id>       Task to rename."
		);
	}
}
