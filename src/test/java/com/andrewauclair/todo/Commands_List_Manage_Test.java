// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Commands_List_Manage_Test {
	private static final String NL = System.lineSeparator();
	
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final Tasks tasks = new Tasks(writer, osInterface);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
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
		commands.execute("create-list test-tasks");
		
		assertTrue(commands.hasListWithName("test-tasks"));
	}
	
	@Test
	void switch_to_another_list() {
		commands.execute("create-list test-tasks");
		commands.execute("switch-list test-tasks");
		
		assertEquals("test-tasks", commands.getListName());
	}
	
	@Test
	void each_list_has_its_own_set_of_tasks() {
		tasks.addTask("default List Task 1");
		tasks.addTask("default List Task 2");
		
		commands.execute("create-list test-tasks");
		commands.execute("switch-list test-tasks");
		
		tasks.addTask("test-tasks List Task 1");
		tasks.addTask("test-tasks List Task 2");
		
		commands.execute("switch-list default");
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "default List Task 1"),
				new Task(2, "default List Task 2")
		);
		
		commands.execute("switch-list test-tasks");
		
		assertThat(tasks.getTasks()).containsOnly(
				new Task(3, "test-tasks List Task 1"),
				new Task(4, "test-tasks List Task 2")
		);
	}
}
