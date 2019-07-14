// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_InactiveTask_Finish_Test extends TaskBaseTestCase {
	@BeforeEach
	void setup() throws IOException {
		super.setup();
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

		Task task = tasks.finishTask(1);

		Task finishedTask = new Task(1, "Testing tasks", Task.TaskState.Finished, Collections.emptyList());
		assertThat(tasks.getTasks()).containsOnly(
				finishedTask,
				new Task(2, "Testing tasks 2", Task.TaskState.Active, Collections.singletonList(new TaskTimes(1234)))
		);

		assertEquals(finishedTask, task);
	}

	@Test
	void finishing_a_specific_task_does_not_reset_the_active_task() {
		Task oldTask = tasks.startTask(1);

		tasks.finishTask(2);

		assertEquals(oldTask, tasks.getActiveTask());
	}

	@Test
	void finish_with_no_active_task_does_not_throw_exception() {
		Task finishedTask = tasks.finishTask(2);

		assertEquals(new Task(2, "Testing tasks 2", Task.TaskState.Finished, Collections.emptyList()), finishedTask);
	}

	@Test
	void finishing_task_tells_task_writer_to_write_file() {
		Task task = tasks.finishTask(1);

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}

	@Test
	void finishing_task_tells_git_control_to_add_file_and_commit() {
		tasks.finishTask(2);

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Finished task 2 - 'Testing tasks 2'\"");
	}

	@Test
	void if_task_does_not_exist_then_an_exception_is_thrown() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.finishTask(3));

		assertEquals("Task not found.", runtimeException.getMessage());
	}
}
