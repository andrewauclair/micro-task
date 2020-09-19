// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

public class Commands_Group_Test extends CommandsBaseTestCase {
	@Test
	void group_command_lists_groups_and_lists_in_the_current_group() {
		tasks.addList(newList("charlie"), true);
		tasks.addList(newList("bravo"), true);
		tasks.addList(newList("alpha"), true);
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addList(newList("/test/one/two"), true);

		commands.execute(printStream, "group");

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
	void group_command_lists_groups_and_lists_in_another_group() {
		tasks.addList(newList("charlie"), true);
		tasks.addList(newList("bravo"), true);
		tasks.addList(newList("alpha"), true);
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addList(newList("/test/one/two"), true);

		tasks.setCurrentGroup(existingGroup("/test/"));

		commands.execute(printStream, "group --group /");

		assertOutput(
				"Group '/'",
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
		tasks.setCurrentGroup(existingGroup("/one/two/"));
		tasks.addList(newList("charlie"), true);
		tasks.addList(newList("bravo"), true);
		tasks.addList(newList("alpha"), true);

		commands.execute(printStream, "group");

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
	void group_command_hides_finished_lists() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/two"), true);

		tasks.finishList(existingList("/test/two"));

		tasks.setCurrentGroup(existingGroup("/test/"));

		commands.execute(printStream, "group");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  one",
				""
		);
	}

	@Test
	void group_command_hides_finished_groups() {
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addGroup(newGroup("/test/two/"));

		tasks.setCurrentGroup(existingGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/two/"));

		commands.execute(printStream, "group");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  one/",
				""
		);
	}

	@Test
	void group_command_with_finished_parameter_displays_finished_lists_and_groups() {
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addGroup(newGroup("/test/two/"));
		tasks.addList(newList("/test/three"), true);
		tasks.addList(newList("/test/four"), true);

		tasks.setCurrentGroup(existingGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/two/"));
		tasks.finishList(existingList("/test/four"));

		commands.execute(printStream, "group --finished");

		assertOutput(
				"Current group is '/test/'",
				"",
				"  four",
				"  two/",
				""
		);
	}

	@Test
	void group_command_help() {
		commands.execute(printStream, "group --help");

		assertOutput(
				"Usage:  group [-h] [--finished] [-g=<group>]",
				"      --finished        List finished tasks.",
				"  -g, --group=<group>   List tasks in this group.",
				"  -h, --help            Show this help message."
		);
	}
}
