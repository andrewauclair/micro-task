// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_List_Test extends CommandsBaseTestCase {
	@Test
	void list_command_lists_groups_and_lists_in_the_current_group() {
		tasks.addList("charlie", true);
		tasks.addList("bravo", true);
		tasks.addList("alpha", true);
		tasks.addList("/test/one/two", true);

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/'",
				"",
				"  alpha",
				"  bravo",
				"  charlie",
				"* " + ANSI_FG_GREEN + "default" + ANSI_RESET,
				"  test/",
				""
		);
	}

	@Test
	void list_groups_and_lists_for_nested_group() {
		tasks.addList("none", true);

		tasks.createGroup("/one/two/");
		tasks.createGroup("/one/three/");
		tasks.switchGroup("/one/two/");
		tasks.addList("charlie", true);
		tasks.addList("bravo", true);
		tasks.addList("alpha", true);

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/one/two/'",
				"",
				"  alpha",
				"  bravo",
				"  charlie",
				""
		);
	}

	@Test
	void list_command_hides_finished_lists() {
		tasks.addList("/test/one", true);
		tasks.addList("/test/two", true);

		tasks.finishList("/test/two");

		tasks.switchGroup("/test/");

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  one",
				""
		);
	}

	@Test
	void list_command_hides_finished_groups() {
		tasks.addGroup("/test/one/");
		tasks.addGroup("/test/two/");

		tasks.switchGroup("/test/");

		tasks.finishGroup("/test/two/");

		commands.execute(printStream, "list");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  one/",
				""
		);
	}

	@Test
	void list_command_with_finished_parameter_displays_finished_lists_and_groups() {
		tasks.addGroup("/test/one/");
		tasks.addGroup("/test/two/");
		tasks.addList("/test/three", true);
		tasks.addList("/test/four", true);

		tasks.switchGroup("/test/");

		tasks.finishGroup("/test/two/");
		tasks.finishList("/test/four");

		commands.execute(printStream, "list --finished");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  four",
				"  two/",
				""
		);
	}

	@Test
	void list_command_help() {
		commands.execute(printStream, "list --help");

		assertOutput(
				"Usage:  list [-h] [--all] [--finished] [--group] [--recursive] [--tasks]",
				"             [--list=<list>]",
				"List tasks or the content of a group.",
				"      --all           List all tasks.",
				"      --finished      List finished tasks.",
				"      --group         List tasks in this group.",
				"  -h, --help          Show this help message.",
				"      --list=<list>   List tasks on this list.",
				"      --recursive     List tasks recursively in all sub-groups.",
				"      --tasks         List tasks on list or in group."
		);
	}
}
