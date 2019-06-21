// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Start_Test extends TaskBaseTestCase {
	@Test
	void starting_task_assigns_it_as_the_active_task() {
		tasks.addTask("Empty task");
		Task task = tasks.addTask("Testing task start command");
		
		Task newActiveTask = tasks.startTask(task.id);
		
		Task oldTask = new Task(2, "Testing task start command", Task.TaskState.Inactive);
		Task activeTask = new Task(2, "Testing task start command", Task.TaskState.Active);
		
		assertEquals(activeTask, tasks.getActiveTask());
		assertEquals(tasks.getActiveTask(), newActiveTask);
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(activeTask);
	}
	
	@Test
	void starting_non_existent_id_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.startTask(5));
		
		assertEquals("com.andrewauclair.todo.Task 5 was not found.", runtimeException.getMessage());
	}
	
	@Test
	void starting_task_tells_task_writer_to_write_file() {
		tasks.addTask("Testing task add command 1");
		tasks.addTask("Testing task add command 2");
		
		Mockito.reset(writer);
		
		Task task2 = tasks.startTask(1);
		
		Mockito.verify(writer).writeTask(task2, "git-data/1.txt");
	}
	
	@Test
	void starting_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Testing task add command 1");
		tasks.addTask("Testing task add command 2");
		
		Mockito.reset(osInterface);
		
		tasks.startTask(2);
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand(new GitCommand("git add 2.txt"));
		order.verify(osInterface).runGitCommand(new GitCommand("git commit -m \"Started task 2 - \\\"Testing task add command 2\\\"\""));
	}
	
	@Test
	void starting_a_task_tracks_the_time_second_it_was_started() {
		tasks.addTask("Testing Task");
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		Task task = tasks.startTask(1);
		
//		TaskTimes.Times times = new TaskTimes.Times(0);
		assertEquals(1234, task.start);
	}
}
