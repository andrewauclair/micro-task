// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.task.ActiveContext.NO_ACTIVE_TASK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Active_Test extends TaskBaseTestCase {
	@Test
	void returns_active_task() {
		tasks.addTask("Testing 1");
		tasks.addTask("Testing 2");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(existingID(2), false);

		Task activeTask = newTask(existingID(2), "Testing 2", TaskState.Active, 2000, Collections.singletonList(new TaskTimes(1234)));

		assertEquals(activeTask, tasks.getActiveTask());
	}

	@Test
	void active_task_returns_correct_task_when_its_on_a_different_list() {
		tasks.addTask("Task 1");

		Task task = tasks.startTask(existingID(1), false);
		
		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		Task activeTask = tasks.getActiveTask();

		assertEquals(task, activeTask);
	}

	@Test
	void no_active_task_throws_exception_with_message() {
		TaskException taskException = assertThrows(TaskException.class, tasks::getActiveTask);
		
		assertEquals("No active task.", taskException.getMessage());
	}

	@Test
	void no_active_task() {
		assertEquals(NO_ACTIVE_TASK, tasks.getActiveTaskID());
	}
}
