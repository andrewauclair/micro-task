// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Finish_Test extends CommandsBaseTestCase {
	@Test
	void finish_command_help() {
		commands.execute(printStream, "finish --help");

		assertOutput(
				"Usage:  finish (-t=<id>[,<id>...] [-t=<id>[,<id>...]]... | -l=<list> |",
				"               -g=<group> | -a) [-h]",
				"  -a, --active",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -t, --task=<id>[,<id>...]",
				""
		);
	}
}
