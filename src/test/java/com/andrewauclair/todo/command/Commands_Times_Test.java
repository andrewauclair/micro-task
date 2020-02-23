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
				"Usage:  times [-h] [--all-time] [--proj-feat] [--today] [--week] [--yesterday]",
				"              [-d=<day>] [-m=<month>] [-y=<year>] [--group=<group>]...",
				"              [--list=<list>]...",
				"      --all-time",
				"  -d, --day=<day>",
				"      --group=<group>",
				"  -h, --help            Show this help message.",
				"      --list=<list>",
				"  -m, --month=<month>",
				"      --proj-feat",
				"      --today",
				"      --week",
				"  -y, --year=<year>",
				"      --yesterday"
		);
	}
}
