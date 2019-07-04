// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.ConsoleColors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_List_Lists_Test extends CommandsBaseTestCase {
	@Test
	void list_lists_lists_all_available_lists() {
		tasks.addList("todo-app-tasks");
		tasks.addList("test");
		tasks.addList("abcd");

		commands.execute("list --lists");

		assertEquals("  abcd" + Utils.NL +
						"* " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN +
						"default" + ConsoleColors.ANSI_RESET +
						Utils.NL +
						"  test" + Utils.NL +
						"  todo-app-tasks" + Utils.NL + Utils.NL
				, outputStream.toString());
	}
}
