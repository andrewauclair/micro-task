// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Move_Group_Test extends CommandsBaseTestCase {
	@Test
	void move_group_from_one_group_to_another() {
		tasks.createGroup("/one/");
		tasks.createGroup("/two/");

		commands.execute(printStream, "move --group /one/ --dest-group /two/");

		assertFalse(tasks.hasGroupPath("/one/"));
		assertTrue(tasks.hasGroupPath("/two/one/"));

		assertOutput(
				"Moved group '/one/' to group '/two/'",
				""
		);
	}

	@Test
	void move_group_to_root_group() {
		tasks.createGroup("/one/two/");

		commands.execute(printStream, "move --group /one/two/ --dest-group /");

		assertFalse(tasks.hasGroupPath("/one/two/"));
		assertTrue(tasks.hasGroupPath("/two/"));

		assertOutput(
				"Moved group '/one/two/' to group '/'",
				""
		);
	}

	@Test
	void move_group_requires_dest_group() {
		commands.execute(printStream, "move --group /one/");

		assertOutput(
				"move --group requires --dest-group",
				""
		);
	}
}
