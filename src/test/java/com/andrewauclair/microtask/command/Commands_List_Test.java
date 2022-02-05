// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_List_Test extends CommandsBaseTestCase {
	@Test
	void list_command_lists_progress_of_current_list() {
		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}
		tasks.finishTask(existingID(1));
		tasks.finishTask(existingID(2));

		commands.execute(printStream, "list");

		assertOutput(
				"Current list is '/default'",
				"",
				"Progress: 2 / 10 [==        ] 20 %",
				""
		);
	}

	@Test
	void list_command_lists_progress_of_a_different_list() {
		for (int i = 0; i < 10; i++) {
			tasks.addTask("Test");
		}
		tasks.finishTask(existingID(1));
		tasks.finishTask(existingID(2));

		tasks.addList(newList("/one"), true);
		tasks.setCurrentList(existingList("/one"));

		commands.execute(printStream, "list --list /default");

		assertOutput(
				"List '/default'",
				"",
				"Progress: 2 / 10 [==        ] 20 %",
				""
		);
	}

	@Test
	void list_command_help() {
		commands.execute(printStream, "list --help");

		assertOutput(
				"Usage:  list [-h] [-l=<list>]",
				"List tasks or the content of a group.",
				"  -h, --help          Show this help message.",
				"  -l, --list=<list>   List tasks on list."
		);
	}
}
