// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Change_List_Test extends CommandsBaseTestCase {
	@Test
	void switch_to_another_list() {
		tasks.addList(newList("test-tasks"), true);

		commands.execute(printStream, "ch -l test-tasks");

		assertEquals(existingList("/test-tasks"), tasks.getCurrentList());

		assertOutput(
				"Switched to list '/test-tasks'",
				""
		);
	}

	@Test
	void switch_to_absolute_path_list() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		commands.execute(printStream, "ch -l /test/one");

		assertEquals(existingList("/test/one"), tasks.getCurrentList());

		assertOutput(
				"Switched to list '/test/one'",
				""
		);
	}

	@Test
	void switch_to_nested_list() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setCurrentGroup(existingGroup("/test/"));

		commands.execute(printStream, "ch -l one");

		assertOutput(
				"Switched to list '/test/one'",
				""
		);
	}

	@Test
	void switching_lists_switches_to_group_of_active_list() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		commands.execute(printStream, "ch -l /test/one");

		assertEquals("/test/", tasks.getCurrentGroup().getFullPath());
	}

	@Test
	void can_not_switch_to_a_list_that_does_not_exist() {
		commands.execute(printStream, "ch -l test");

		assertOutput(
				"Invalid value for option '--list': List '/test' does not exist.",
				""
		);
		assertEquals(existingList("/default"), tasks.getCurrentList());
		assertEquals("/", tasks.getCurrentGroup().getFullPath());
	}

	@Test
	void switch_list_is_always_lower_case() {
		tasks.addList(newList("random"), true);

		commands.execute(printStream, "ch -l ranDOM");

		assertOutput(
				"Switched to list '/random'",
				""
		);
	}

	@Test
	void switch_list_does_not_exist_is_always_lower_case() {
		commands.execute(printStream, "ch -l ranDOM");

		assertOutput(
				"Invalid value for option '--list': List '/random' does not exist.",
				""
		);
	}

	@Test
	void set_active_list_in_local_settings_when_changing_lists() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		commands.execute(printStream, "ch -l test/one");

		Mockito.verify(localSettings).setActiveList(existingList("/test/one"));
		Mockito.verify(localSettings).setActiveGroup(existingGroup("/test/"));
	}

	@Test
	void invalid_list_path() {
		commands.execute(printStream, "ch -l /projects/test/");

		assertOutput(
				"Invalid value for option '--list': List name must not end in /",
				""
		);
	}
}
