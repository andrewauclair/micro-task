// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Tasks_Stop_Test {
	private final Tasks tasks = new Tasks(new TaskWriter(new FileCreatorMock()), Mockito.mock(OSInterface.class));
	
	@Test
	void stop_command_sets_the_active_task_to_none() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		tasks.startTask(1);
		
		assertEquals(new Task(1, "Test 2", Task.TaskState.Active), tasks.getActiveTask());
		
		Task stoppedTask = tasks.stopTask();
		
		ActiveTaskAsserts.assertNoActiveTask(tasks);
		
		// TODO This should check if it is inactive
		assertEquals(new Task(1, "Test 2", Task.TaskState.Active), stoppedTask);
	}
	
	@Test
	void stop_command_throws_exception_if_there_is_no_active_task() {
		ActiveTaskAsserts.assertThrowsNoActiveTaskException(tasks::stopTask);
	}
}
