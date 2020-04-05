// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Make_List_Test extends CommandsBaseTestCase {
	@Test
	void create_new_list_of_tasks() {
		commands.execute(printStream, "mk -l test-tasks");

		assertTrue(tasks.hasListWithName("/test-tasks"));

		assertOutput(
				"Created new list '/test-tasks'",
				""
		);
	}

	@Test
	void create_absolute_path_list() {
		commands.execute(printStream, "mk -l /test/one");

		assertTrue(tasks.hasListWithName("/test/one"));

		assertOutput(
				"Created new list '/test/one'",
				""
		);
	}

	@Test
	void create_nested_relative_list() {
		commands.execute(printStream, "mk -g /test/one/");

		tasks.switchGroup("/test/one/");

		outputStream.reset();

		commands.execute(printStream, "mk -l two");

		assertOutput(
				"Created new list '/test/one/two'",
				""
		);
	}

	@Test
	void can_not_create_a_new_list_with_a_name_that_already_exists() {
		tasks.addList("test", true);

		commands.execute(printStream, "mk -l test");

		assertOutput(
				"List '/test' already exists.",
				""
		);
	}

	@Test
	void create_list_is_always_lower_case() {
		commands.execute(printStream, "mk -l RaNDOm");

		assertTrue(tasks.hasListWithName("/random"));

		assertOutput(
				"Created new list '/random'",
				""
		);
	}

	@Test
	void create_list_already_exists_is_always_lower_case() {
		tasks.addList("random", true);

		commands.execute(printStream, "mk -l RaNDOm");

		assertTrue(tasks.hasListWithName("/random"));

		assertOutput(
				"List '/random' already exists.",
				""
		);
	}
}
