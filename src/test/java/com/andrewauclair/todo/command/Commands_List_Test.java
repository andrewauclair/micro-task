// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

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
}
