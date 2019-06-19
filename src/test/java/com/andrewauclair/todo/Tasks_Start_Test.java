// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Start_Test {
	private final Tasks tasks = new Tasks(new TaskWriter(new FileCreatorMock()), Mockito.mock(OSInterface.class));

	@Test
	void starting_task_assigns_it_as_the_active_task() {
		tasks.addTask("Empty task");
		Task task = tasks.addTask("Testing task start command");

		Task newActiveTask = tasks.startTask(task.id);

		// TODO This should check the state being active
		assertEquals(new Task(1, "Testing task start command", Task.TaskState.Inactive), tasks.getActiveTask());
		assertEquals(tasks.getActiveTask(), newActiveTask);
	}

	@Test
	void starting_non_existent_id_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.startTask(5));

		assertEquals("com.andrewauclair.todo.Task 5 was not found.", runtimeException.getMessage());
	}
}
