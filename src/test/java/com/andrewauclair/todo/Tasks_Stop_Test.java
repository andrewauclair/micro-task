// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Tasks_Stop_Test extends TaskBaseTestCase {
	@Test
	void stop_command_sets_the_active_task_to_none() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		Task oldTask = tasks.startTask(2);
		
		Task expectedOldTask = new Task(2, "Test 2", Task.TaskState.Active, new TaskTimes.Times(1234));
		assertEquals(expectedOldTask, tasks.getActiveTask());
		assertEquals(expectedOldTask, oldTask);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);
		
		Task stoppedTask = tasks.stopTask();
		
		ActiveTaskAsserts.assertNoActiveTask(tasks);
		
		assertEquals(new Task(2, "Test 2", Task.TaskState.Inactive, new TaskTimes.Times(1234, 4567)), stoppedTask);
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(stoppedTask);
	}
	
	@Test
	void stop_command_throws_exception_if_there_is_no_active_task() {
		ActiveTaskAsserts.assertThrowsNoActiveTaskException(tasks::stopTask);
	}
	
	@Test
	void stopping_task_tells_task_writer_to_write_file() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		tasks.startTask(1);
		
		Mockito.reset(writer);
		
		Task task = tasks.stopTask();
		
		Mockito.verify(writer).writeTask(task, "git-data/1.txt");
	}
	
	@Test
	void stopping_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		tasks.startTask(2);
		
		Mockito.reset(osInterface);
		
		tasks.stopTask();
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand(new GitCommand("git add 2.txt"));
		order.verify(osInterface).runGitCommand(new GitCommand("git commit -m \"Stopped task 2 - \\\"Test 2\\\"\""));
	}
	
	@Test
	void stopping_a_task_records_the_stop_time() {
		tasks.addTask("Test 1");
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		tasks.startTask(1);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);
		
		Task task = tasks.stopTask();
		
		assertThat(task.times.asList()).containsOnly(new TaskTimes.Times(1234, 4567));
	}
	
	@Test
	void multiple_stops_result_in_multiple_times() {
		tasks.addTask("Test 1");
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);
		
		tasks.startTask(1);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(2345L);
		
		Task stop1 = tasks.stopTask();
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(3456L);
		
		tasks.startTask(1);
		
		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);
		
		Task stop2 = tasks.stopTask();
		
		assertThat(stop1.times.asList()).containsOnly(
				new TaskTimes.Times(1234, 2345)
		);
		
		assertThat(stop2.times.asList()).containsOnly(
				new TaskTimes.Times(1234, 2345),
				new TaskTimes.Times(3456, 4567)
		);
	}
}
