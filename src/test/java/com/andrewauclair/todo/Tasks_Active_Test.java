// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Active_Test {
	private final Tasks tasks = new Tasks(new TaskWriter(new FileCreatorMock()), Mockito.mock(OSInterface.class));
	
	@Test
	void returns_active_task() {
		tasks.addTask("Testing 1");
		tasks.addTask("Testing 2");
		
		tasks.startTask(1);
		
		Task activeTask = new Task(1, "Testing 2", Task.TaskState.Active);
		
		assertEquals(activeTask, tasks.getActiveTask());
	}
	
	@Test
	void no_active_task_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::getActiveTask);
		
		assertEquals("No active task.", runtimeException.getMessage());
	}
}
