// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Tasks_Stop_Test {
	private final Tasks tasks = new Tasks(new TaskWriter(new FileCreatorMock()), Mockito.mock(OSInterface.class));
	
	@Test
	void stop_command_sets_the_active_task_to_none() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		Task oldTask = tasks.startTask(1);
		
		Task expectedOldTask = new Task(1, "Test 2", Task.TaskState.Active);
		assertEquals(expectedOldTask, tasks.getActiveTask());
		assertEquals(expectedOldTask, oldTask);
		
		Task stoppedTask = tasks.stopTask();
		
		ActiveTaskAsserts.assertNoActiveTask(tasks);
		
		assertEquals(new Task(1, "Test 2", Task.TaskState.Inactive), stoppedTask);
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(stoppedTask);
	}
	
	@Test
	void stop_command_throws_exception_if_there_is_no_active_task() {
		ActiveTaskAsserts.assertThrowsNoActiveTaskException(tasks::stopTask);
	}
}
