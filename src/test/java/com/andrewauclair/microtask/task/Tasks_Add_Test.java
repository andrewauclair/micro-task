// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class Tasks_Add_Test extends TaskBaseTestCase {
	@Test
	void adding_task_adds_it_to_a_list() {
		Task actualTask = tasks.addTask("Testing task add command");

		Task expectedTask = newTask(1, "Testing task add command", TaskState.Inactive, 1000);

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
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		Task task = tasks.addTask("Testing task");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/test/1.txt");
	}

	@Test
	void adding_task_tells_git_control_to_add_file_and_commit() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Testing task add command 1");
		
		order.verify(osInterface).gitCommit("Added task 1 - 'Testing task add command 1'");

		tasks.addTask("Testing task add command 2");
		
		order.verify(osInterface).gitCommit("Added task 2 - 'Testing task add command 2'");
	}

	@Test
	void adding_task_tells_git_control_to_add_file_to_current_list_directory() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		tasks.addTask("Testing task add command 1");
		
		order.verify(osInterface).gitCommit("Added task 1 - 'Testing task add command 1'");
	}

	@Test
	void tasks_allows_us_to_add_an_actual_task_object_for_reloading_from_a_file() {
		Task task = newTask(4, "Testing", TaskState.Inactive, 0);
		tasks.addTask(task);

		assertThat(tasks.getTasks()).containsOnly(task);
	}

	@Test
	void write_next_id_prints_exception_to_output() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);

		tasks.addTask("Test");
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}

	@Test
	void user_can_finish_the_active_task_when_it_is_on_a_different_list() {
		tasks.addTask("Task 1");

		Task task = tasks.startTask(existingID(1), false);
		
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		tasks.addTask("Task 2");

		Task finishedTask = tasks.finishTask();

		assertEquals(task.id, finishedTask.id);
	}

	@Test
	void on_finish_the_active_task_is_finished_on_the_correct_list() {
		tasks.addTask("Task 1");

		tasks.startTask(existingID(1), false);
		
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		tasks.addTask("Task 2");

		Task finishedTask = tasks.finishTask();

		tasks.setCurrentList(existingList("default"));

		assertThat(tasks.getTasks()).containsOnly(finishedTask);
	}

	@Test
	void adding_task_that_is_active_sets_it_as_the_active_task() {
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		Task task = newTask(1, "Test", TaskState.Active, 1000);

		tasks.addTask(task);

		assertEquals(1, tasks.getActiveTask().id);
	}
	
	@Test
	void adding_task_with_quotes_in_name_creates_git_message_with_escaped_quotes() {
		tasks.addTask("Test \"Quotes\"");
		
		Mockito.verify(osInterface).gitCommit("Added task 1 - 'Test \\\"Quotes\\\"'");
	}

	@Test
	void adding_task_to_a_specific_list() {
		tasks.addList(newList("one"), true);
		tasks.addTask("Test", existingList("one"));

		assertThat(tasks.getTasksForList(existingList("one"))).containsOnly(
				newTask(1, "Test", TaskState.Inactive, 1000)
		);
	}

	@Test
	void adding_task_to_specific_list_tells_task_writer_to_write_file() {
		tasks.addList(newList("one"), true);

		Task task1 = tasks.addTask("Testing task add command 1", existingList("one"));
		Task task2 = tasks.addTask("Testing task add command 2", existingList("one"));

		Mockito.verify(writer).writeTask(task1, "git-data/tasks/one/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/one/2.txt");
	}

	@Test
	void adding_tasks_to_specific_list_tells_writer_to_write_next_id_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/next-id.txt")).thenReturn(new DataOutputStream(outputStream));
		
		tasks.addList(newList("one"), true);
		tasks.addTask("Test", existingList("one"));

		assertEquals("2", outputStream.toString());
	}

	@Test
	void adding_task_to_specific_list_tells_git_control_to_add_file_and_commit() {
		tasks.addList(newList("test"), true);

		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Testing task add command 1", existingList("test"));
		
		order.verify(osInterface).gitCommit("Added task 1 - 'Testing task add command 1'");

		tasks.addTask("Testing task add command 2", existingList("test"));
		
		order.verify(osInterface).gitCommit("Added task 2 - 'Testing task add command 2'");
	}

	@Test
	void add_task_to_nested_list() {
		tasks.createGroup(newGroup("/test/one/"));
		tasks.setCurrentGroup(existingGroup("/test/one/"));
		tasks.addList(newList("two"), true);
		tasks.setCurrentList(existingList("two"));

		tasks.addTask(newTask(1, "Test", TaskState.Inactive, 0));

		assertThat(tasks.getTasksForList(existingList("/test/one/two"))).containsOnly(
				newTask(1, "Test", TaskState.Inactive, 0)
		);
	}

	@Test
	void add_throws_exception_if_the_list_has_been_finished() {
		tasks.addList(newList("one"), true);
		tasks.finishList(existingList("one"));

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addTask("Test", existingList("one")));

		assertEquals("Task 'Test' cannot be created because list '/one' has been finished.", taskException.getMessage());

		assertThat(tasks.getListByName(existingList("/one")).getTasks()).isEmpty();
	}

	@Test
	void add_to_specific_list_throws_exception_if_list_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addTask("Test", existingList("one")));
		
		assertEquals("List '/one' does not exist.", taskException.getMessage());
	}

	@Test
	void add_throws_exception_if_task_with_id_already_exists() {
		tasks.addTask(newTask(1, "Test throw", TaskState.Inactive, 0));
		
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.addTask(newTask(1, "Throws here", TaskState.Inactive, 0)));
		
		assertEquals("Task with ID 1 already exists.", taskException.getMessage());
	}

	@Test
	void add_throws_exception_if_task_with_id_already_exists_on_a_different_list() {
		tasks.addTask("Test");
		tasks.addList(newList("one"), true);
		tasks.setCurrentList(existingList("one"));
		
		TaskException runtimeException = assertThrows(TaskException.class, () -> tasks.addTask(newTask(1, "Throws here", TaskState.Inactive, 0)));

		assertEquals("Task with ID 1 already exists.", runtimeException.getMessage());
	}

	@Test
	void next_id_increments_after_add() {
		tasks.addTask("Test");

		assertEquals(2, tasks.nextID());
	}
}
