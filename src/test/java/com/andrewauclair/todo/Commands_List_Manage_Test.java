// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Commands_List_Manage_Test {
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final Tasks tasks = new Tasks(1, writer, osInterface);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new ByteArrayOutputStream());
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
		commands.execute("create-list test-tasks");
		
		assertTrue(commands.hasListWithName("test-tasks"));
		
		assertEquals("Created new list \"test-tasks\"" + Utils.NL, outputStream.toString());
	}
	
	@Test
	void switch_to_another_list() {
		commands.execute("create-list test-tasks");
		
		outputStream.reset();
		
		commands.execute("switch-list test-tasks");
		
		assertEquals("test-tasks", commands.getListName());
		
		assertEquals("Switched to list \"test-tasks\"" + Utils.NL, outputStream.toString());
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
	
	@Test
	void can_not_create_a_new_list_with_a_name_that_already_exists() {
		commands.execute("create-list test");
		
		outputStream.reset();
		
		commands.execute("create-list test");
		
		assertEquals("List \"test\" already exists." + Utils.NL, outputStream.toString());
	}
	
	@Test
	void can_not_switch_to_a_list_that_does_not_exist() {
		commands.execute("switch-list test");
		
		assertEquals("List \"test\" does not exist." + Utils.NL, outputStream.toString());
		assertEquals("default", tasks.getCurrentList());
	}
	
	// TODO I think these style tests can be parameterized
	@Test
	void switch_list_without_a_task_number_prints_invalid_command() {
		commands.execute("switch-list");
		
		assertEquals("Invalid command." + Utils.NL, outputStream.toString());
	}
	
	@Test
	void switch_list_with_too_many_arguments_prints_invalid_command() {
		commands.execute("switch-list test two");
		
		assertEquals("Invalid command." + Utils.NL, outputStream.toString());
	}
	
	@Test
	void create_list_without_a_task_number_prints_invalid_command() {
		commands.execute("create-list");
		
		assertEquals("Invalid command." + Utils.NL, outputStream.toString());
	}
	
	@Test
	void create_list_with_too_many_arguments_prints_invalid_command() {
		commands.execute("create-list test two");
		
		assertEquals("Invalid command." + Utils.NL, outputStream.toString());
	}
}
