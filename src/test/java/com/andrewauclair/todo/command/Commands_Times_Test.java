// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Commands_Times_Test extends CommandsBaseTestCase {
	@Test
	void day_under_1_is_invalid() {
		commands.execute(printStream, "times -d -1");
		
		assertOutput(
				"Day option must be 1 - 31",
				""
		);
	}
	
	@Test
	void day_over_31_is_invalid() {
		commands.execute(printStream, "times -d 32");
		
		assertOutput(
				"Day option must be 1 - 31",
				""
		);
	}
	
	@Test
	void month_under_1_is_invalid() {
		commands.execute(printStream, "times -d 1 -m 0");
		
		assertOutput(
				"Month option must be 1 - 12",
				""
		);
	}
	
	@Test
	void month_over_12_is_invalid() {
		commands.execute(printStream, "times -d 1 -m 13");
		
		assertOutput(
				"Month option must be 1 - 12",
				""
		);
	}

	@Test
	void proj_feat_output_equals() {
		EqualsVerifier.forClass(TimesCommand.ProjFeatOutput.class).verify();
	}

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
