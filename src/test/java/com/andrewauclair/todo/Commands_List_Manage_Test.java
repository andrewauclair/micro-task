// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.GitLabReleases;
import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Commands_List_Manage_Test {
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final PrintStream printStream = new PrintStream(outputStream);
	private final Tasks tasks = new Tasks(1, writer, printStream, osInterface);
	private final GitLabReleases gitLabReleases = Mockito.mock(GitLabReleases.class);
	private final Commands commands = new Commands(tasks, gitLabReleases);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new DataOutputStream(new ByteArrayOutputStream()));
	}

	@Test
	void starting_list_is_called_default() {
		assertEquals("default", commands.getListName());
	}

	@Test
	void name_of_uncreated_list_is_not_found() {
		assertFalse(commands.hasListWithName("test-tasks"));
	}

	@Test
	void has_default_list() {
		assertTrue(commands.hasListWithName("default"));
	}

	@Test
	void create_new_list_of_tasks() {
		commands.execute(printStream, "create-list test-tasks");

		assertTrue(commands.hasListWithName("test-tasks"));

		assertEquals("Created new list 'test-tasks'" + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void switch_to_another_list() {
		commands.execute(printStream, "create-list test-tasks");

		outputStream.reset();
		
		commands.execute(printStream, "switch-list test-tasks");

		assertEquals("test-tasks", commands.getListName());

		assertEquals("Switched to list 'test-tasks'" + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void each_list_has_its_own_set_of_tasks() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");
		
		commands.execute(printStream, "create-list test-tasks");
		commands.execute(printStream, "switch-list test-tasks");

		tasks.addTask("test-tasks List Task 1");
		tasks.addTask("test-tasks List Task 2");
		
		commands.execute(printStream, "switch-list default");

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "default List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(0))),
				new Task(2, "default List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))
		);
		
		commands.execute(printStream, "switch-list test-tasks");

		assertThat(tasks.getTasks()).containsOnly(
				new Task(3, "test-tasks List Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(0))),
				new Task(4, "test-tasks List Task 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))
		);
	}

	@Test
	void can_not_create_a_new_list_with_a_name_that_already_exists() {
		commands.execute(printStream, "create-list test");

		outputStream.reset();
		
		commands.execute(printStream, "create-list test");

		assertEquals("List 'test' already exists." + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void can_not_switch_to_a_list_that_does_not_exist() {
		commands.execute(printStream, "switch-list test");

		assertEquals("List 'test' does not exist." + Utils.NL + Utils.NL, outputStream.toString());
		assertEquals("default", tasks.getCurrentList());
	}

	// TODO I think these style tests can be parameterized
	@Test
	void switch_list_without_a_task_number_prints_invalid_command() {
		commands.execute(printStream, "switch-list");

		assertEquals("Invalid command." + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void switch_list_with_too_many_arguments_prints_invalid_command() {
		commands.execute(printStream, "switch-list test two");

		assertEquals("Invalid command." + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void create_list_without_a_task_number_prints_invalid_command() {
		commands.execute(printStream, "create-list");

		assertEquals("Invalid command." + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void create_list_with_too_many_arguments_prints_invalid_command() {
		commands.execute(printStream, "create-list test two");

		assertEquals("Invalid command." + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void create_list_is_always_lower_case() {
		commands.execute(printStream, "create-list RaNDOm");

		assertTrue(commands.hasListWithName("random"));

		assertEquals("Created new list 'random'" + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void create_list_already_exists_is_always_lower_case() {
		tasks.addList("random");
		
		commands.execute(printStream, "create-list RaNDOm");

		assertTrue(commands.hasListWithName("random"));

		assertEquals("List 'random' already exists." + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void switch_list_is_always_lower_case() {
		tasks.addList("random");
		
		commands.execute(printStream, "switch-list ranDOM");

		assertEquals("Switched to list 'random'" + Utils.NL + Utils.NL, outputStream.toString());
	}

	@Test
	void switch_list_does_not_exist_is_always_lower_case() {
		commands.execute(printStream, "switch-list ranDOM");

		assertEquals("List 'random' does not exist." + Utils.NL + Utils.NL, outputStream.toString());
	}
}
