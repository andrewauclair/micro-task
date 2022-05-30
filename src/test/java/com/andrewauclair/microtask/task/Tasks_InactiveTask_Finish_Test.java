// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_InactiveTask_Finish_Test extends TaskBaseTestCase {
	@BeforeEach
	protected void setup() throws IOException {
		super.setup();
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
	}

	@Test
	void finishing_a_task_removes_it_from_the_task_list() {
		assertThat(tasks.getTasks()).containsOnly(
				TestUtils.existingTask(existingID(1), "Testing tasks", TaskState.Inactive, 1000).build(),
				TestUtils.existingTask(existingID(2), "Testing tasks 2", TaskState.Inactive, 2000).build()
		);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(existingID(2), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task task = tasks.finishTask(existingID(1));
		
		Task finishedTask = TestUtils.existingTask(existingID(1), "Testing tasks", TaskState.Finished, 1000, 4567, Collections.emptyList()).build();

		assertThat(tasks.getTasks()).containsOnly(
				finishedTask,
				TestUtils.existingTask(existingID(2), "Testing tasks 2", TaskState.Active, 2000, Collections.singletonList(new TaskTimes(1234)))
		);

		assertEquals(finishedTask, task);
	}

	@Test
	void finishing_a_specific_task_does_not_reset_the_active_task() {
		Task oldTask = tasks.startTask(existingID(1), false);

		tasks.finishTask(existingID(2));

		assertEquals(oldTask, tasks.getActiveTask());
	}

	@Test
	void finish_with_no_active_task_does_not_throw_exception() {
		Task finishedTask = tasks.finishTask(existingID(2));
		
		assertEquals(TestUtils.existingTask(existingID(2), "Testing tasks 2", TaskState.Finished, 2000, 3000, Collections.emptyList()).build(), finishedTask);
	}

	@Test
	void finishing_task_tells_task_writer_to_write_file() throws IOException {
		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/archive.txt")).thenReturn(archiveStream);

		Task task = tasks.finishTask(existingID(1));

		Mockito.verify(writer).writeTask(task, archiveStream);
	}

	@Test
	void finishing_task_tells_git_control_to_add_file_and_commit() {
		tasks.finishTask(existingID(2));

		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).gitCommit("Finished task 2 - 'Testing tasks 2'");
	}

	@Test
	void if_task_does_not_exist_then_an_exception_is_thrown() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.finishTask(existingID(3)));
		
		assertEquals("Task 3 does not exist.", taskException.getMessage());
	}
}
