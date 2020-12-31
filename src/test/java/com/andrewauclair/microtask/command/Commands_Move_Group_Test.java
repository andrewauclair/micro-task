// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskGroupFinder;
import com.andrewauclair.microtask.task.TaskGroupName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Move_Group_Test extends CommandsBaseTestCase {
	@Test
	void move_group_from_one_group_to_another() {
		tasks.createGroup(newGroup("/one/"));
		tasks.createGroup(newGroup("/two/"));

		commands.execute(printStream, "move --group /one/ --dest-group /two/");

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertFalse(finder.hasGroupPath(new TaskGroupName(tasks, "/one/")));
		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/two/one/")));

		assertOutput(
				"Moved group '/one/' to group '/two/'",
				""
		);
	}

	@Test
	void move_group_to_root_group() {
		tasks.createGroup(newGroup("/one/two/"));

		commands.execute(printStream, "move --group /one/two/ --dest-group /");

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertFalse(finder.hasGroupPath(new TaskGroupName(tasks, "/one/two/")));
		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/two/")));

		assertOutput(
				"Moved group '/one/two/' to group '/'",
				""
		);
	}

	@Test
	void move_group_requires_dest_group() {
		tasks.createGroup(newGroup("/one/"));

		commands.execute(printStream, "move --group /one/");

		assertOutput(
				"move --group requires --dest-group",
				""
		);
	}
}
