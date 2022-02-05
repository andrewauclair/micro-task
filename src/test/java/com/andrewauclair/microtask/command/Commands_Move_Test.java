// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void move_command_help() {
		commands.execute(printStream, "move --help");

		assertOutput(
				"Usage:  move [-h] [--interactive] [--dest-group=<dest_group>]",
				"             [--dest-list=<dest_list>] (-t=<id>[,<id>...] [-t=<id>[,",
				"             <id>...]]... | -l=<list> | -g=<group>)",
				"Move a task, list or group.",
				"      --dest-group=<dest_group>",
				"                        Destination group for list or group.",
				"      --dest-list=<dest_list>",
				"                        Destination list for task.",
				"  -g, --group=<group>   Group to move.",
				"  -h, --help            Show this help message.",
				"      --interactive     Prompt y/n per task.",
				"  -l, --list=<list>     List to move.",
				"  -t, --task=<id>[,<id>...]",
				"                        Tasks to move."
		);
	}
}
