// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Move_Test extends TaskBaseTestCase {
	@Test
	void moving_task_moves_it_to_the_new_list() {
		Task task = tasks.addTask("Task to move");
		tasks.addList("one");

		tasks.moveTask(1, "one");

		assertThat(tasks.getTasksForList("default"))
				.isEmpty();

		assertThat(tasks.getTasksForList("one"))
				.containsOnly(task);
	}

	@Test
	void moving_task_deletes_the_current_task_files_in_the_folder() {
		tasks.addTask("Test 1");
		tasks.addList("one");

		tasks.moveTask(1, "one");

		Mockito.verify(osInterface).removeFile("git-data/tasks/default/1.txt");

	}

	@Test
	void moving_task_writes_new_task_files_into_new_folder() {
		tasks.addTask("Test 1");
		tasks.addList("one");

		Task task = tasks.moveTask(1, "one");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/one/1.txt");
	}

	@Test
	void moving_task_tells_git_control_to_add_new_task_file_and_commit() {
		tasks.addTask("Test 1");
		tasks.addList("one");

		InOrder order = Mockito.inOrder(osInterface);

		tasks.moveTask(1, "one");

		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git add tasks/one/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Moved task 1 - 'Test 1' to list '/one'\"");
	}

	@Test
	void can_move_task_on_different_list() {
		Task task = tasks.addTask("Task to move");
		tasks.addList("one");
		tasks.setActiveList("one");

		tasks.moveTask(1, "one");

		assertThat(tasks.getTasksForList("default"))
				.isEmpty();

		assertThat(tasks.getTasksForList("one"))
				.containsOnly(task);
	}
	
	@Test
	void moving_the_active_task_changes_active_list() {
		tasks.addTask("Test 1");
		tasks.addList("one");

		tasks.startTask(1, false);
		
		tasks.moveTask(1, "one");

		assertEquals("/one", tasks.getActiveTaskList());
	}
	
	@Test
	void moving_inactive_task_does_not_change_active_task_list() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addList("one");

		tasks.startTask(2, false);
		
		tasks.moveTask(1, "one");

		assertEquals("/default", tasks.getActiveTaskList());
	}

	@Test
	void moving_list_moves_folder_of_files() throws IOException {
		tasks.addList("/test/one");

		InOrder order = Mockito.inOrder(osInterface);

		tasks.moveList("/test/one", "/");

		order.verify(osInterface).moveFolder("/test/one", "/one");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Moved list '/test/one' to group '/'\"");
	}

	@Test
	void moving_group_moves_folder_of_files() throws IOException {
		tasks.createGroup("/one");
		tasks.createGroup("/two");

		Mockito.reset(osInterface);

		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.moveGroup("/one/", "/two/");
		
		order.verify(osInterface).moveFolder("/one/", "/two/one/");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Moved group '/one/' to group '/two/'\"");
	}

	@Test
	void moving_active_list_changes_active_list_name() {
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");

		tasks.moveList("/test/one", "/");

		assertEquals("/one", tasks.getActiveList());
	}

	@Test
	void moving_active_group_changes_active_group_name() {
		tasks.createGroup("/one/");
		tasks.createGroup("/two/");
		
		tasks.switchGroup("/one/");
		
		tasks.moveGroup("/one/", "/two/");
		
		assertEquals("/two/one/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void throws_exception_trying_to_move_list_that_does_not_exist() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.moveList("/test/one", "/"));

		assertEquals("List '/test/one' does not exist.", runtimeException.getMessage());
	}

	@Test
	void throws_exception_when_destination_group_does_not_exist() {
		tasks.addList("one");

		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.moveList("/one", "/test"));

		assertEquals("Group '/test' does not exist.", runtimeException.getMessage());
	}

	@Test
	void throws_exception_trying_to_move_group_that_does_not_exist() {
		tasks.createGroup("/two");

		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.moveGroup("/one", "/two"));

		assertEquals("Group '/one' does not exist.", runtimeException.getMessage());
	}

	@Test
	void throws_exception_trying_to_move_to_group_that_does_not_exist() {
		tasks.createGroup("/one/");
		
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.moveGroup("/one/", "/two/"));
		
		assertEquals("Group '/two/' does not exist.", runtimeException.getMessage());
	}

	@Test
	void catch_moveFolder_io_exception_in_moveList() throws IOException {
		Mockito.doThrow(IOException.class).when(osInterface).moveFolder(Mockito.anyString(), Mockito.anyString());

		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");

		tasks.moveList("/test/one", "/");

		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
	
	@Test
	void catch_moveFolder_io_exception_in_moveGroup() throws IOException {
		Mockito.doThrow(IOException.class).when(osInterface).moveFolder(Mockito.anyString(), Mockito.anyString());
		
		tasks.createGroup("/test/one/");
		tasks.switchGroup("/test/one/");
		
		tasks.moveGroup("/test/one/", "/");
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
	
	@Test
	void throws_exception_if_task_was_not_found() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.moveTask(5, "one"));
		assertEquals("Task 5 was not found.", runtimeException.getMessage());
	}

	@Test
	void moving_task_throws_exception_if_move_to_list_is_not_found() {
		tasks.addTask("Test 1");

		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.moveTask(1, "one"));
		assertEquals("List '/one' was not found.", runtimeException.getMessage());
	}
}
