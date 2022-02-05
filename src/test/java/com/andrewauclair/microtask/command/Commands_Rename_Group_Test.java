// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskGroupFinder;
import com.andrewauclair.microtask.task.TaskGroupName;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Rename_Group_Test extends CommandsBaseTestCase {
	@Test
	void rename_a_group() {
		tasks.addGroup(newGroup("/one/"));

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/one/")));

		commands.execute(printStream, "rename group /one/ -n \"/two/\"");

		assertFalse(finder.hasGroupPath(new TaskGroupName(tasks, "/one/")));
		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/two/")));

		assertOutput(
				"Renamed group '/one/' to '/two/'",
				""
		);
	}

	@Test
	void renaming_active_group_sets_active_group_to_new_group_name() {
		tasks.addGroup(newGroup("/one/"));
		tasks.setCurrentGroup(existingGroup("/one/"));

		commands.execute(printStream, "rename group /one/ -n \"/two/\"");

		assertEquals("/two/", tasks.getCurrentGroup().getFullPath());
	}

	@Test
	void renaming_parent_adds_child_list_to_new_parent() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/test"), true);

		tasks.setCurrentList(existingList("/one/test"));
		tasks.addTask("Test");

		commands.execute(printStream, "rename group /one/ -n \"/two/\"");

		assertEquals("/two/test", tasks.getListForTask(existingID(1)).getFullPath());
	}

	@Test
	void renaming_parent_adds_child_group_to_new_parent() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addList(newList("/one/two/three"), true);

		tasks.setCurrentList(existingList("/one/two/three"));
		tasks.addTask("Test");

		commands.execute(printStream, "rename group /one/ -n \"/test/\"");

		assertEquals("/test/two/three", tasks.getListForTask(existingID(1)).getFullPath());
	}

	@Test
	void rename_group__old_group_name_should_end_in_slash() {
		tasks.addGroup(newGroup("/one/"));

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new ExistingGroupName(tasks, "/one/")));

		commands.execute(printStream, "rename group /one -n \"/two/\"");

		assertOutput(
				"Invalid value for positional parameter at index 0 (<group>): Group name must end in /",
				""
		);
	}

	@Test
	void rename_group__new_group_name_should_end_in_slash() {
		tasks.addGroup(newGroup("/one/"));

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new ExistingGroupName(tasks, "/one/")));

		commands.execute(printStream, "rename group /one/ -n \"/two\"");

		assertOutput(
				"Invalid value for option '--name': Group name must end in /",
				""
		);
	}
}
