// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Rename_Group_Test extends CommandsBaseTestCase {
	@Test
	void rename_a_group() {
		tasks.addGroup("/one/");

		assertTrue(tasks.hasGroupPath("/one/"));

		commands.execute(printStream, "rename --group /one/ -n \"/two/\"");

		assertFalse(tasks.hasGroupPath("/one/"));
		assertTrue(tasks.hasGroupPath("/two/"));

		assertOutput(
				"Renamed group '/one/' to '/two/'",
				""
		);
	}

	@Test
	void renaming_active_group_sets_active_group_to_new_group_name() {
		tasks.addGroup("/one/");
		tasks.switchGroup("/one/");

		commands.execute(printStream, "rename --group /one/ -n \"/two/\"");

		assertEquals("/two/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void renaming_parent_adds_child_list_to_new_parent() {
		tasks.addList("/one/test", true);

		tasks.setActiveList("/one/test");
		tasks.addTask("Test");

		commands.execute(printStream, "rename --group /one/ -n \"/two/\"");

		assertEquals("/two/test", tasks.getListForTask(1).getFullPath());
	}

	@Test
	void renaming_parent_adds_child_group_to_new_parent() {
		tasks.addList("/one/two/three", true);

		tasks.setActiveList("/one/two/three");
		tasks.addTask("Test");

		commands.execute(printStream, "rename --group /one/ -n \"/test/\"");

		assertEquals("/test/two/three", tasks.getListForTask(1).getFullPath());
	}

	@Test
	void rename_group__old_group_name_should_end_in_slash() {
		tasks.addGroup("/one/");

		assertTrue(tasks.hasGroupPath("/one/"));

		commands.execute(printStream, "rename --group /one -n \"/two/\"");

		assertOutput(
				"Old group name should end with /",
				""
		);
	}

	@Test
	void rename_group__new_group_name_should_end_in_slash() {
		tasks.addGroup("/one/");

		assertTrue(tasks.hasGroupPath("/one/"));

		commands.execute(printStream, "rename --group /one/ -n \"/two\"");

		assertOutput(
				"New group name should end with /",
				""
		);
	}
}
