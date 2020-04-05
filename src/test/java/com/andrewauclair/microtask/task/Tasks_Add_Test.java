// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class Tasks_Add_Test extends TaskBaseTestCase {
	@Test
	void adding_task_adds_it_to_a_list() {
		Task actualTask = tasks.addTask("Testing task add command");

		Task expectedTask = new Task(1, "Testing task add command", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)));

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
		
		Mockito.when(osInterface.createOutputStream("git-data/next-id.txt")).thenReturn(new DataOutputStream(outputStream));

		tasks.addTask("Test");

		assertEquals("2", outputStream.toString());
	}

	@Test
	void task_is_saved_to_a_folder_for_the_current_list() {
		tasks.addList("test", true);
		tasks.setActiveList("test");

		Task task = tasks.addTask("Testing task");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/test/1.txt");
	}

	@Test
	void adding_task_tells_git_control_to_add_file_and_commit() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Testing task add command 1");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 1 - 'Testing task add command 1'\"");

		tasks.addTask("Testing task add command 2");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 2 - 'Testing task add command 2'\"");
	}

	@Test
	void adding_task_tells_git_control_to_add_file_to_current_list_directory() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList("test", true);
		tasks.setActiveList("test");

		tasks.addTask("Testing task add command 1");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/test/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 1 - 'Testing task add command 1'\"");
	}

	@Test
	void tasks_allows_us_to_add_an_actual_task_object_for_reloading_from_a_file() {
		Task task = new Task(4, "Testing", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		tasks.addTask(task);

		assertThat(tasks.getTasks()).containsOnly(task);
	}

	@Test
	void write_next_id_prints_exception_to_output() throws IOException {
		DataOutputStream outputStream = Mockito.mock(DataOutputStream.class);
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(outputStream);

		Mockito.doThrow(IOException.class).when(outputStream).write(Mockito.any());

		tasks.addTask("Test");
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}

	@Test
	void user_can_finish_the_active_task_when_it_is_on_a_different_list() {
		tasks.addTask("Task 1");

		Task task = tasks.startTask(1, false);
		
		tasks.addList("test", true);
		tasks.setActiveList("test");

		tasks.addTask("Task 2");

		Task finishedTask = tasks.finishTask();

		assertEquals(task.id, finishedTask.id);
	}

	@Test
	void on_finish_the_active_task_is_finished_on_the_correct_list() {
		tasks.addTask("Task 1");

		tasks.startTask(1, false);
		
		tasks.addList("test", true);
		tasks.setActiveList("test");

		tasks.addTask("Task 2");

		Task finishedTask = tasks.finishTask();

		tasks.setActiveList("default");

		assertThat(tasks.getTasks()).containsOnly(finishedTask);
	}

	@Test
	void adding_task_that_is_active_sets_it_as_the_active_task() {
		tasks.addList("test", true);
		tasks.setActiveList("test");

		Task task = new Task(1, "Test", TaskState.Active, Collections.singletonList(new TaskTimes(1000)));

		tasks.addTask(task);

		assertEquals(1, tasks.getActiveTask().id);
	}
	
	@Test
	void adding_task_with_quotes_in_name_creates_git_message_with_escaped_quotes() {
		tasks.addTask("Test \"Quotes\"");
		
		Mockito.verify(osInterface).runGitCommand("git commit -m \"Added task 1 - 'Test \\\"Quotes\\\"'\"");
	}

	@Test
	void adding_task_to_a_specific_list() {
		tasks.addList("one", true);
		tasks.addTask("Test", "one");

		assertThat(tasks.getTasksForList("one")).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);
	}

	@Test
	void adding_task_to_specific_list_tells_task_writer_to_write_file() {
		tasks.addList("one", true);

		Task task1 = tasks.addTask("Testing task add command 1", "one");
		Task task2 = tasks.addTask("Testing task add command 2", "one");

		Mockito.verify(writer).writeTask(task1, "git-data/tasks/one/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/one/2.txt");
	}

	@Test
	void adding_tasks_to_specific_list_tells_writer_to_write_next_id_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/next-id.txt")).thenReturn(new DataOutputStream(outputStream));
		
		tasks.addList("one", true);
		tasks.addTask("Test", "one");

		assertEquals("2", outputStream.toString());
	}

	@Test
	void adding_task_to_specific_list_tells_git_control_to_add_file_and_commit() {
		tasks.addList("test", true);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Testing task add command 1", "test");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/test/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 1 - 'Testing task add command 1'\"");

		tasks.addTask("Testing task add command 2", "test");
		
		order.verify(osInterface).runGitCommand("git add next-id.txt");
		order.verify(osInterface).runGitCommand("git add tasks/test/2.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Added task 2 - 'Testing task add command 2'\"");
	}

	@Test
	void add_task_to_nested_list() {
		tasks.createGroup("/test/one/");
		tasks.switchGroup("/test/one/");
		tasks.addList("two", true);
		tasks.setActiveList("two");

		tasks.addTask(new Task(1, "Test", TaskState.Inactive, Collections.emptyList()));

		assertThat(tasks.getTasksForList("/test/one/two")).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.emptyList())
		);
	}

	@Test
	void add_to_specific_list_throws_exception_if_list_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addTask("Test", "one"));
		
		assertEquals("List '/one' does not exist.", taskException.getMessage());
	}

	@Test
	void add_throws_exception_if_task_with_id_already_exists() {
		tasks.addTask(new Task(1, "Test throw", TaskState.Inactive, Collections.singletonList(new TaskTimes(0))));
		
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addTask(new Task(1, "Throws here", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))));
		
		assertEquals("Task with ID 1 already exists.", taskException.getMessage());
	}

	@Test
	void add_throws_exception_if_task_with_id_already_exists_on_a_different_list() {
		tasks.addTask("Test");
		tasks.addList("one", true);
		tasks.setActiveList("one");
		
		TaskException runtimeException = assertThrows(TaskException.class, () -> tasks.addTask(new Task(1, "Throws here", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)))));

		assertEquals("Task with ID 1 already exists.", runtimeException.getMessage());
	}
	
	@Test
	void add_throws_exception_if_group_does_not_exist() {
		TaskException runtimeException = assertThrows(TaskException.class, () -> tasks.addTask("Test", "/one/two"));
		
		assertEquals("Group '/one/' does not exist.", runtimeException.getMessage());
	}

	@Test
	void next_id_increments_after_add() {
		tasks.addTask("Test");

		assertEquals(2, tasks.nextID());
	}
}
