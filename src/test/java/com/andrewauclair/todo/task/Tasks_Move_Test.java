// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

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
		tasks.setCurrentList("one");

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
