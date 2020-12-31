// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskListFinder;
import com.andrewauclair.microtask.task.TaskListName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Add_List_Test extends CommandsBaseTestCase {
	@Test
	void create_new_list_of_tasks() {
		commands.execute(printStream, "add list test-tasks");
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		TaskListName listName = new TaskListName(tasks, "/test-tasks") {};
		
		assertTrue(finder.hasList(listName));

		assertOutput(
				"Created list '/test-tasks'",
				""
		);
	}

	@Test
	void create_absolute_path_list() {
		tasks.addGroup(newGroup("/test/"));
		
		commands.execute(printStream, "add list /test/one");
		
		TaskListName listName = new TaskListName(tasks, "/test/one") {};
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		assertTrue(finder.hasList(listName));

		assertOutput(
				"Created list '/test/one'",
				""
		);
	}

	@Test
	void create_nested_relative_list() {
		commands.execute(printStream, "add group /test/one/");

		tasks.setCurrentGroup(existingGroup("/test/one/"));

		outputStream.reset();

		commands.execute(printStream, "add list two");

		assertOutput(
				"Created list '/test/one/two'",
				""
		);
	}

	@Test
	void can_not_create_a_new_list_with_a_name_that_already_exists() {
		tasks.addList(newList("test"), true);

		commands.execute(printStream, "add list test");

		assertOutput(
				"Invalid value for positional parameter at index 0 (<list>): List '/test' already exists.",
				""
		);
	}

	@Test
	void create_list_is_always_lower_case() {
		commands.execute(printStream, "add list RaNDOm");
		
		TaskListName listName = new TaskListName(tasks, "/random") {};
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		assertTrue(finder.hasList(listName));

		assertOutput(
				"Created list '/random'",
				""
		);
	}

	@Test
	void create_list_already_exists_is_always_lower_case() {
		tasks.addList(newList("random"), true);

		commands.execute(printStream, "add list RaNDOm");
		
		TaskListName listName = new TaskListName(tasks, "/random") {};
		
		TaskListFinder finder = new TaskListFinder(tasks);
		
		assertTrue(finder.hasList(listName));

		assertOutput(
				"Invalid value for positional parameter at index 0 (<list>): List '/random' already exists.",
				""
		);
	}
}
