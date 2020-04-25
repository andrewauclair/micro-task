// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Rename_List_Test extends CommandsBaseTestCase {
	@Test
	void renaming_a_list() {
		tasks.addList(newList("test"), true);

		commands.execute(printStream, "rename list test -n \"new-name\"");

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/new-name");

		assertOutput(
				"Renamed list '/test' to '/new-name'",
				""
		);
	}

	@Test
	void list_renames_are_always_relative() {
		tasks.addGroup(newGroup("/test/new/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/new/two"), true);
		tasks.setActiveGroup(existingGroup("/test/"));

		commands.execute(printStream, "rename list /test/one -n \"two\"");

		commands.execute(printStream, "rename list one -n \"/test/two\"");

		commands.execute(printStream, "rename list new/two -n \"three\"");

		commands.execute(printStream, "rename list one -n \"test/two\"");

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test/one", "/test/new/two");

		assertOutput(
				"Lists must be renamed with name, not paths.",
				"",
				"Lists must be renamed with name, not paths.",
				"",
				"Lists must be renamed with name, not paths.",
				"",
				"Lists must be renamed with name, not paths.",
				""
		);
	}
}
