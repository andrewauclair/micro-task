// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Commands_Times_Test extends CommandsBaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void times_command_help(String parameter) {
		commands.execute(printStream, "times " + parameter);

		assertOutput(
				"Usage:  times [-h] [--proj-feat] [--summary] [--tasks] [--today] [-d=<day>]",
				"              [--list=<list>] [-m=<month>] [--task=<id>] [-y=<year>]",
				"  -d, --day=<day>",
				"  -h, --help            Show this help message.",
				"      --list=<list>",
				"  -m, --month=<month>",
				"      --proj-feat",
				"      --summary",
				"      --task=<id>",
				"      --tasks",
				"      --today",
				"  -y, --year=<year>"
		);
	}
}
