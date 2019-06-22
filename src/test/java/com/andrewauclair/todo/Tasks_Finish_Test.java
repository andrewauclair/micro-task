// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Finish_Test extends TaskBaseTestCase {
	@BeforeEach
	void setup() {
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
	}
	
	@Test
	void finishing_a_task_removes_it_from_the_task_list() {
		assertThat(tasks.getTasks()).containsOnly(new Task(1, "Testing tasks"),
				new Task(2, "Testing tasks 2"));
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		tasks.startTask(2);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);
		
		Task task = tasks.finishTask();
		
		Task finishedTask = new Task(2, "Testing tasks 2", Task.TaskState.Finished, new TaskTimes(1234, 4567));
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Testing tasks"),
				finishedTask
		);
		
		assertEquals(finishedTask, task);
	}
	
	@Test
	void finishing_a_task_resets_the_active_task() {
		Task oldTask = tasks.startTask(1);
		Task finishedTask = tasks.finishTask();
		
		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::getActiveTask);
		
		assertEquals("No active task.", runtimeException.getMessage());
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(finishedTask);
	}
	
	@Test
	void finish_with_no_active_task_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::finishTask);
		
		assertEquals("No active task.", runtimeException.getMessage());
	}
	
	@Test
	void finishing_task_tells_task_writer_to_write_file() {
		tasks.startTask(1);
		
		Mockito.reset(writer);
		
		Task task = tasks.finishTask();
		
		Mockito.verify(writer).writeTask(task, "git-data/1.txt");
	}
	
	@Test
	void finishing_task_tells_git_control_to_add_file_and_commit() {
		tasks.startTask(2);
		
		Mockito.reset(osInterface);
		
		tasks.finishTask();
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand(new GitCommand("git add 2.txt"));
		order.verify(osInterface).runGitCommand(new GitCommand("git commit -m \"Finished task 2 - \\\"Testing tasks 2\\\"\""));
	}
	
	@Test
	void finishing_a_task_records_the_stop_time() {
		tasks.addTask("Test 1");
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		tasks.startTask(1);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);
		
		Task task = tasks.finishTask();
		
		assertThat(task.getTimes()).containsOnly(new TaskTimes(1234, 4567));
	}
	
	@Test
	void finish_respects_the_multiple_starts_and_stops_before_it() {
		tasks.addTask("Test 1");
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		tasks.startTask(1);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(2345L);
		
		Task stop = tasks.stopTask();
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(3456L);
		
		tasks.startTask(1);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);
		
		Task finishTask = tasks.finishTask();
		
		assertThat(stop.getTimes()).containsOnly(
				new TaskTimes(1234, 2345)
		);
		
		assertThat(finishTask.getTimes()).containsOnly(
				new TaskTimes(1234, 2345),
				new TaskTimes(3456, 4567)
		);
	}
}
