// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_List_Test extends CommandsBaseTestCase {
	@Test
	void list_command_lists_groups_and_lists_in_the_current_group() {
		tasks.addList(newList("charlie"), true);
		tasks.addList(newList("bravo"), true);
		tasks.addList(newList("alpha"), true);
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addList(newList("/test/one/two"), true);

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
		tasks.addList(newList("none"), true);

		tasks.createGroup(newGroup("/one/two/"));
		tasks.createGroup(newGroup("/one/three/"));
		tasks.setActiveGroup(existingGroup("/one/two/"));
		tasks.addList(newList("charlie"), true);
		tasks.addList(newList("bravo"), true);
		tasks.addList(newList("alpha"), true);

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
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/two"), true);

		tasks.finishList(existingList("/test/two"));

		tasks.setActiveGroup(existingGroup("/test/"));

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
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addGroup(newGroup("/test/two/"));

		tasks.setActiveGroup(existingGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/two/"));

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
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addGroup(newGroup("/test/two/"));
		tasks.addList(newList("/test/three"), true);
		tasks.addList(newList("/test/four"), true);

		tasks.setActiveGroup(existingGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/two/"));
		tasks.finishList(existingList("/test/four"));

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
				"Usage:  list [-h] [--all] [--current-group] [--finished] [--recursive]",
				"             [--tasks] [-g=<group>] [-l=<list>]",
				"List tasks or the content of a group.",
				"      --all             List all tasks.",
				"      --current-group   List tasks in the current group.",
				"      --finished        List finished tasks.",
				"  -g, --group=<group>   List tasks in this group.",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>     List tasks on list.",
				"      --recursive       List tasks recursively in all sub-groups.",
				"      --tasks           List tasks on list or in group."
		);
	}
}
