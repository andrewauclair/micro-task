// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Commands_Rename_Test extends CommandsBaseTestCase {
	@Test
	void rename_command_help() {
		commands.execute(printStream, "rename --help");

		assertOutput(
				"Usage:  rename (-l=<list> | -g=<group> | -t=<id>) [-h] -n=<name>",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -n, --name=<name>",
				"  -t, --task=<id>"
		);
	}
}
