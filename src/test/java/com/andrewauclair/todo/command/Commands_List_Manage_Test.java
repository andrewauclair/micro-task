// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Commands_List_Manage_Test extends CommandsBaseTestCase {
	@Test
	void starting_list_is_called_default() {
		assertEquals("/default", tasks.getActiveList());
	}

	@Test
	void name_of_uncreated_list_is_not_found() {
		assertFalse(tasks.hasListWithName("/test-tasks"));
	}

	@Test
	void has_default_list() {
		assertTrue(tasks.hasListWithName("/default"));
	}

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
		commands.execute(printStream, "ch -g /test/one/");

		outputStream.reset();

		commands.execute(printStream, "mk -l two");

		assertOutput(
				"Created new list '/test/one/two'",
				""
		);
	}

	@Test
	void switch_to_another_list() {
		commands.execute(printStream, "mk -l test-tasks");

		outputStream.reset();

		commands.execute(printStream, "ch -l test-tasks");

		assertEquals("/test-tasks", tasks.getActiveList());

		assertOutput(
				"Switched to list '/test-tasks'",
				""
		);
	}

	@Test
	void switch_to_absolute_path_list() {
		commands.execute(printStream, "mk -l /test/one");

		outputStream.reset();

		commands.execute(printStream, "ch -l /test/one");

		assertEquals("/test/one", tasks.getActiveList());

		assertOutput(
				"Switched to list '/test/one'",
				""
		);
	}

	@Test
	void switch_to_nested_list() {
		commands.execute(printStream, "mk -l /test/one");
		commands.execute(printStream, "ch -g /test/");

		outputStream.reset();

		commands.execute(printStream, "ch -l one");

		assertOutput(
				"Switched to list '/test/one'",
				""
		);
	}
	
	@Test
	void switching_lists_switches_to_group_of_active_list() {
		commands.execute(printStream, "mk -l /test/one");
		commands.execute(printStream, "ch -l /test/one");
		
		assertEquals("/test/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void each_list_has_its_own_set_of_tasks() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");

		commands.execute(printStream, "mk -l test-tasks");
		commands.execute(printStream, "ch -l test-tasks");

		tasks.addTask("test-tasks List Task 1");
		tasks.addTask("test-tasks List Task 2");

		commands.execute(printStream, "ch -l default");

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "default List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				new Task(2, "default List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(2000)))
		);

		commands.execute(printStream, "ch -l test-tasks");

		assertThat(tasks.getTasks()).containsOnly(
				new Task(3, "test-tasks List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(3000))),
				new Task(4, "test-tasks List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(4000)))
		);
	}

	@Test
	void can_not_create_a_new_list_with_a_name_that_already_exists() {
		commands.execute(printStream, "mk -l test");

		outputStream.reset();

		commands.execute(printStream, "mk -l test");

		assertOutput(
				"List '/test' already exists.",
				""
		);
	}

	@Test
	void can_not_switch_to_a_list_that_does_not_exist() {
		commands.execute(printStream, "ch -l test");

		assertOutput(
				"List '/test' does not exist.",
				""
		);
		assertEquals("/default", tasks.getActiveList());
		assertEquals("/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void switch_list_without_a_task_number_prints_invalid_command() {
		commands.execute(printStream, "ch -l");

		assertOutput(
				"Missing required parameter for option '--list' (<list>)",
				""
		);
	}

	@Test
	void switch_list_with_too_many_arguments_prints_invalid_command() {
		commands.execute(printStream, "ch -l test two");

		assertOutput(
				"Unmatched argument at index 3: 'two'",
				""
		);
	}

	@Test
	void create_list_without_a_list_name_prints_invalid_command() {
		commands.execute(printStream, "mk -l");

		assertOutput(
				"Missing required parameter for option '--list' (<list>)",
				""
		);
	}

	@Test
	void create_list_with_too_many_arguments_prints_invalid_command() {
		commands.execute(printStream, "mk -l test two");

		assertOutput(
				"Unmatched argument at index 3: 'two'",
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

	@Test
	void switch_list_is_always_lower_case() {
		tasks.addList("random", true);

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
				"List '/random' does not exist.",
				""
		);
	}
}
