// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Active_Test extends TaskBaseTestCase {
	@Test
	void returns_active_task() {
		tasks.addTask("Testing 1");
		tasks.addTask("Testing 2");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(2);

		Task activeTask = new Task(2, "Testing 2", Task.TaskState.Active, Collections.singletonList(new TaskTimes(1234)));

		assertEquals(activeTask, tasks.getActiveTask());
	}

	@Test
	void active_task_returns_correct_task_when_its_on_a_different_list() {
		tasks.addTask("Task 1");

		Task task = tasks.startTask(1);

		tasks.addList("test");
		tasks.setCurrentList("test");

		Task activeTask = tasks.getActiveTask();

		assertEquals(task, activeTask);
	}

	@Test
	void no_active_task_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::getActiveTask);

		assertEquals("No active task.", runtimeException.getMessage());
	}
}
