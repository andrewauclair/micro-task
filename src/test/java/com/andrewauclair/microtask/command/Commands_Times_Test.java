// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

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

	@Test
	void times_command_help() {
		commands.execute(printStream, "times --help");

		assertOutput(
				"Usage:  times [-h] [--all-month] [--all-time] [--log] [--proj-feat] [--today]",
				"              [--total] [--week] [--yesterday] [-d=<day>] [-m=<month>]",
				"              [-y=<year>] [--group=<group>]... [--list=<list>]...",
				"Display times for tasks, lists or groups.",
				"      --all-month       Display times for the entire month",
				"      --all-time        Display all task times recorded.",
				"  -d, --day=<day>       Day to display times for.",
				"      --group=<group>   The group to display times for.",
				"  -h, --help            Show this help message.",
				"      --list=<list>     The list to display times for.",
				"      --log",
				"  -m, --month=<month>   Month to display times for.",
				"      --proj-feat       Display times grouped as projects and features.",
				"      --today           Display times for today.",
				"      --total           Display only the final total.",
				"      --week            Week to display times for.",
				"  -y, --year=<year>     Year to display times for.",
				"      --yesterday       Display times for yesterday."
		);
	}
}
