// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Finish_Test extends CommandsBaseTestCase {
	@Test
	void finish_command_help() {
		commands.execute(printStream, "finish --help");

		assertOutput(
				"Usage:  finish (-t=<id>[,<id>...] [-t=<id>[,<id>...]]... | -l=<list> |",
				"               -g=<group> | --active-task) [-h]",
				"Finish a task, list or group.",
				"      --active-task     Finish the active task.",
				"  -g, --group=<group>   Group to finish.",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>     List to finish.",
				"  -t, --task=<id>[,<id>...]",
				"                        Task(s) to finish."
		);
	}
}
