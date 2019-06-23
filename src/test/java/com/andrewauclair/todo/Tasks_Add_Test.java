// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Tasks_Add_Test extends TaskBaseTestCase {
	@Test
	void adding_task_adds_it_to_a_list() {
		Task actualTask = tasks.addTask("Testing task add command");
		
		Task expectedTask = new Task(1, "Testing task add command");
		
		assertThat(tasks.getTasks()).containsOnly(expectedTask);
		assertEquals(expectedTask, actualTask);
	}
	
	@Test
	void adding_task_tells_task_writer_to_write_file() {
		Task task1 = tasks.addTask("Testing task add command 1");
		Task task2 = tasks.addTask("Testing task add command 2");
		
		Mockito.verify(writer).writeTask(task1, "git-data/tasks/default/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/default/2.txt");
	}
	
	@Test
	void adding_tasks_tells_write_to_write_next_id_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/next-id.txt")).thenReturn(outputStream);
		
		tasks.addTask("Test");
		
		assertEquals("2", outputStream.toString());
	}
	
	@Test
	void task_is_saved_to_a_folder_for_the_current_list() {
		tasks.addList("test");
		tasks.setCurrentList("test");
		
		Task task = tasks.addTask("Testing task");
		
		Mockito.verify(writer).writeTask(task, "git-data/tasks/test/1.txt");
	}
	
	@Test
	void adding_task_tells_git_control_to_add_file_and_commit() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addTask("Testing task add command 1");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 1 - \\\"Testing task add command 1\\\"\"");
		
		tasks.addTask("Testing task add command 2");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 2 - \\\"Testing task add command 2\\\"\"");
	}
	
	@Test
	void adding_task_tells_git_control_to_add_file_to_current_list_directory() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList("test");
		tasks.setCurrentList("test");
		
		tasks.addTask("Testing task add command 1");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/test/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 1 - \\\"Testing task add command 1\\\"\"");
	}
	
	@Test
	void tasks_allows_us_to_add_an_actual_task_object_for_reloading_from_a_file() {
		Task task = new Task(4, "Testing");
		tasks.addTask(task);
		
		assertThat(tasks.getTasks()).containsOnly(task);
	}
	
	@Test
	void write_next_id_prints_exception_to_output() throws IOException {
		OutputStream outputStream = Mockito.mock(OutputStream.class);
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(outputStream);
		
		Mockito.doThrow(IOException.class).when(outputStream).write(Mockito.any());
		
		tasks.addTask("Test");
		
		assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
}
