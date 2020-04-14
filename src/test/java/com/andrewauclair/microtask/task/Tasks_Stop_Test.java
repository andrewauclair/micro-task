// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Stop_Test extends TaskBaseTestCase {
	@Test
	void stop_command_sets_the_active_task_to_none() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		Task oldTask = tasks.startTask(2, false);

		Task expectedOldTask = new Task(2, "Test 2", TaskState.Active, Arrays.asList(new TaskTimes(2000), new TaskTimes(1234)));
		assertEquals(expectedOldTask, tasks.getActiveTask());
		assertEquals(expectedOldTask, oldTask);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task stoppedTask = tasks.stopTask();

		TaskException taskException = assertThrows(TaskException.class, tasks::getActiveTask, "Expected no active task");

		assertEquals("No active task.", taskException.getMessage());

		assertEquals(new Task(2, "Test 2", TaskState.Inactive, Arrays.asList(new TaskTimes(2000), new TaskTimes(1234, 4567))), stoppedTask);
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(stoppedTask);
	}

	@Test
	void stop_command_throws_exception_if_there_is_no_active_task() {
		TaskException taskException = assertThrows(TaskException.class, tasks::stopTask, "Expected no active task");

		assertEquals("No active task.", taskException.getMessage());
	}

	@Test
	void stopping_task_tells_task_writer_to_write_file() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		Mockito.reset(writer);

		Task task = tasks.stopTask();

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}

	@Test
	void stopping_task_tells_git_control_to_add_file_and_commit() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(2, false);

		Mockito.reset(osInterface);

		tasks.stopTask();

		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Stopped task 2 - 'Test 2'\"");
	}

	@Test
	void stopping_a_task_records_the_stop_time() {
		tasks.addTask("Test 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task task = tasks.stopTask();
		
		assertThat(task.getAllTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, 4567)
		);
	}

	@Test
	void multiple_stops_result_in_multiple_times() {
		tasks.addTask("Test 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(2345L);

		Task stop1 = tasks.stopTask();

		Mockito.when(osInterface.currentSeconds()).thenReturn(3456L);

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task stop2 = tasks.stopTask();
		
		assertThat(stop1.getAllTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, 2345)
		);
		
		assertThat(stop2.getAllTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, 2345),
				new TaskTimes(3456, 4567)
		);
	}

	@Test
	void user_can_stop_the_active_task_when_it_is_on_a_different_list() {
		tasks.addTask("Task 1");

		Task task = tasks.startTask(1, false);
		
		tasks.addList("test", true);
		tasks.setActiveList(existingList("test"));

		tasks.addTask("Task 2");

		Task stoppedTask = tasks.stopTask();

		assertEquals(task.id, stoppedTask.id);
	}

	@Test
	void stopping_task_tells_task_writer_to_write_file_on_correct_list() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1, false);
		
		tasks.addList("test", true);
		tasks.setActiveList(existingList("test"));

		Mockito.reset(writer);

		Task task = tasks.stopTask();

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}

	@Test
	void stopping_task_tells_git_control_to_add_file_and_commit_on_correct_list() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(2, false);
		
		tasks.addList("test", true);
		tasks.setActiveList(existingList("test"));

		Mockito.reset(osInterface);

		tasks.stopTask();

		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Stopped task 2 - 'Test 2'\"");
	}

	@Test
	void on_stop_the_active_task_is_stopped_on_the_correct_list() {
		tasks.addTask("Task 1");

		tasks.startTask(1, false);
		
		tasks.addList("test", true);
		tasks.setActiveList(existingList("test"));

		tasks.addTask("Task 2");

		Task stoppedTask = tasks.stopTask();

		tasks.setActiveList(existingList("default"));

		assertThat(tasks.getTasks()).containsOnly(stoppedTask);
	}

	@Test
	void active_task_list_is_empty_string_after_stopping_task() {
		tasks.addTask("Task 1");

		tasks.startTask(1, false);

		assertEquals("/default", tasks.getActiveTaskList().absoluteName());

		tasks.stopTask();
		
		TaskException taskException = assertThrows(TaskException.class, tasks::getActiveTaskList);
		assertEquals("No active task.", taskException.getMessage());
	}
}
