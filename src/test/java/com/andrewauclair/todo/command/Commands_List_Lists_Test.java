// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_List_Lists_Test extends CommandsBaseTestCase {
	@Test
	void list_lists_lists_all_available_lists() {
		tasks.addList("todo-app-tasks", true);
		tasks.addList("test", true);
		tasks.addList("abcd", true);
		tasks.addList("/test/one/two", true);

		commands.execute(printStream, "list --lists");

		assertOutput(
				"  /abcd",
				"* " + ANSI_FG_GREEN + "/default" + ANSI_RESET,
				"  /test",
				"  /test/one/two",
				"  /todo-app-tasks",
				""
		);
	}
	
	@Test
	void list_lists_ignores_finished_lists() {
		tasks.addList("/test/one", true);
		tasks.addList("/test/two", true);
		
		tasks.finishList("/test/two");
		
		commands.execute(printStream, "list --lists");
		
		assertOutput(
				"* " + ANSI_FG_GREEN + "/default" + ANSI_RESET,
				"  /test/one",
				""
		);
	}
	
	@Test
	void list_lists_only_finished_lists() {
		tasks.addList("/test/one", true);
		tasks.addList("/test/two", true);
		
		tasks.finishList("/test/two");
		
		commands.execute(printStream, "list --lists --finished");
		
		assertOutput(
				"  /test/two",
				""
		);
	}
}
