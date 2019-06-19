// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Finish_Test {
	private final Tasks tasks = new Tasks(new TaskWriter(new FileCreatorMock()), Mockito.mock(OSInterface.class));

	@BeforeEach
	void setup() {
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
	}

	@Test
	void finishing_a_task_removes_it_from_the_task_list() {
		assertThat(tasks.getTasks()).containsOnly(new Task(0, "Testing tasks", Task.TaskState.Inactive),
				new Task(1, "Testing tasks 2", Task.TaskState.Inactive));

		tasks.startTask(1);

		Task task = tasks.finishTask();

		assertThat(tasks.getTasks()).containsOnly(new Task(0, "Testing tasks", Task.TaskState.Inactive));

		assertEquals(new Task(1, "Testing tasks 2", Task.TaskState.Finished), task);
	}

	// TODO This technically works because when we remove the task it won't be found in the list by getActiveTask, this behavior will probably change in the future and this test will fail
	@Test
	void finishing_a_task_resets_the_active_task() {
		tasks.startTask(1);
		tasks.finishTask();

		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::getActiveTask);

		assertEquals("No active task.", runtimeException.getMessage());
	}

	@Test
	void finish_with_no_active_task_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::finishTask);

		assertEquals("No active task.", runtimeException.getMessage());
	}
}
