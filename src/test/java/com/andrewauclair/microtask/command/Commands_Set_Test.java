// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	@Disabled("Waiting until we add this option")
	void set_number_of_hours_in_day() {
		commands.execute(printStream, "set --hours-in-day=6");

		Mockito.verify(localSettings).setHoursInDay(6);

		assertOutput(
				"Set hours in day to 6",
				""
		);
	}

	@Test
	void set_task_command_help() {
		commands.execute(printStream, "set-task --help");

		assertOutput(
				"Usage:  set-task [-hr] [--inactive] [--not-recurring] --task=<id>",
				"  -h, --help            Show this help message.",
				"      --inactive        Set task state to inactive.",
				"      --not-recurring   Set task to non-recurring.",
				"  -r, --recurring       Set task to recurring.",
				"      --task=<id>       Task to set."
		);
	}

	@Test
	void set_list_command_help() {
		commands.execute(printStream, "set-list --help");

		assertOutput(
				"Usage:  set-list ([--in-progress] [[-p=<project>] [-f=<feature>]]) [-h]",
				"                 -l=<list>",
				"  -f, --feature=<feature>   The feature to set.",
				"  -h, --help                Show this help message.",
				"      --in-progress         Set the list state to in progress.",
				"  -l, --list=<list>         The list to set.",
				"  -p, --project=<project>   The project to set."
		);
	}

	@Test
	void set_group_command_help() {
		commands.execute(printStream, "set-group --help");

		assertOutput(
				"Usage:  set-group ([--in-progress] [[-p=<project>] [-f=<feature>]]) [-h]",
				"                  -g=<group>",
				"  -f, --feature=<feature>   The feature to set.",
				"  -g, --group=<group>       The group to set.",
				"  -h, --help                Show this help message.",
				"      --in-progress         Set the list state to in progress.",
				"  -p, --project=<project>   The project to set."
		);
	}
}
