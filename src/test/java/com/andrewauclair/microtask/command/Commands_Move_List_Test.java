// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskListFinder;
import com.andrewauclair.microtask.task.TaskListName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Move_List_Test extends CommandsBaseTestCase {
	@Test
	void move_list_from_one_group_to_another() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList(existingList("/one/two/three"));

		tasks.addTask("Test 1");

		commands.execute(printStream, "move --list /one/two/three --dest-group /one/test/");
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		assertFalse(finder.hasList(new TaskListName(tasks, "/one/two/two") {}));
		assertTrue(finder.hasList(new TaskListName(tasks, "/one/test/three") {}));

		assertOutput(
				"Moved list /one/two/three to group '/one/test/'",
				""
		);
	}

	@Test
	void move_list_to_root_group() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList(existingList("/one/two/three"));

		tasks.addTask("Test 1");

		commands.execute(printStream, "move --list /one/two/three --dest-group /");
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		assertFalse(finder.hasList(new TaskListName(tasks, "/one/two/three") {}));
		assertTrue(finder.hasList(new TaskListName(tasks, "/three") {}));

		assertOutput(
				"Moved list /one/two/three to group '/'",
				""
		);
	}

	@Test
	void move_list_requires_dest_group_not_group_option() {
		tasks.addGroup("/one/");

		commands.execute(printStream, "move --list /default --group /one/");

		assertOutput(
				"Error: --list=<list>, --group=<group> are mutually exclusive (specify only one)",
				""
		);
	}

	@Test
	void move_list_requires_dest_group() {
		tasks.addList("/one", true);

		commands.execute(printStream, "move --list /one");

		assertOutput(
				"move --list requires --dest-group",
				""
		);
	}
}
