// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
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
	void renaming_a_nested_list() {
		tasks.createGroup(newGroup("/test/"));
		tasks.addList(newList("test/one"), true);
		tasks.setCurrentGroup(existingGroup("/test/"));

		commands.execute(printStream, "rename list one -n \"two\"");

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test/two");

		assertOutput(
				"Renamed list '/test/one' to '/test/two'",
				""
		);
	}

	@Test
	void rename_a_list_in_a_different_group() {
		tasks.createGroup(newGroup("/test/"));
		tasks.createGroup(newGroup("/friday/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setCurrentGroup(existingGroup("/friday/"));

		commands.execute(printStream, "rename list /test/one -n \"two\"");

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test/two");

		assertOutput(
				"Renamed list '/test/one' to '/test/two'",
				""
		);
	}

	@Test
	void new_list_must_be_provided_with_no_slashes() {
		tasks.addGroup(newGroup("/test/new/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/new/two"), true);
		tasks.setCurrentGroup(existingGroup("/test/"));

		commands.execute(printStream, "rename list one -n \"/test/two\"");

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test/one", "/test/new/two");

		assertOutput(
				"Lists must be renamed with name, not paths.",
				""
		);
	}
}
