// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void move_command_help() {
		commands.execute(printStream, "move --help");

		assertOutput(
				"Usage:  move (-t=<id>[,<id>...] [-t=<id>[,<id>...]]... | -l=<list> |",
				"             -g=<group>) [-h] [--dest-group=<dest_group>]",
				"             [--dest-list=<dest_list>]",
				"      --dest-group=<dest_group>",
				"",
				"      --dest-list=<dest_list>",
				"",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -t, --task=<id>[,<id>...]",
				""
		);
	}
}
