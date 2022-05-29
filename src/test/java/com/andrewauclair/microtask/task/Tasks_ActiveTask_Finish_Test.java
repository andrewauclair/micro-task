// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.task.ActiveContext.NO_ACTIVE_TASK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_ActiveTask_Finish_Test extends TaskBaseTestCase {
	@BeforeEach
	protected void setup() throws IOException {
		super.setup();
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
	}

	@Test
	void finishing_active_task_clears_active_task_id() {
		tasks.startTask(existingID(1), false);

		assertEquals(1, tasks.getActiveTaskID());
		tasks.finishTask(existingID(1));

		assertEquals(NO_ACTIVE_TASK, tasks.getActiveTaskID());
	}

	@Test
	void finishing_a_task_removes_it_from_the_task_list() {
		assertThat(tasks.getTasks()).containsOnly(
				newTask(existingID(1), "Testing tasks", TaskState.Inactive, 1000).build(),
				newTask(existingID(2), "Testing tasks 2", TaskState.Inactive, 2000).build()
		);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(existingID(2), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task task = tasks.finishTask();
		
		Task finishedTask = newTask(existingID(2), "Testing tasks 2", TaskState.Finished, 2000, 4567, Arrays.asList(new TaskTimes(1234, 4567))).build();

		assertThat(tasks.getTasks()).containsOnly(
				newTask(existingID(1), "Testing tasks", TaskState.Inactive,1000).build(),
				finishedTask
		);

		assertEquals(finishedTask, task);
	}

	@Test
	void finishing_a_task_resets_the_active_task() {
		Task oldTask = tasks.startTask(existingID(1), false);
		Task finishedTask = tasks.finishTask();
		
		TaskException taskException = assertThrows(TaskException.class, tasks::getActiveTask);
		
		assertEquals("No active task.", taskException.getMessage());
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(finishedTask);
	}

	@Test
	void finish_with_no_active_task_throws_exception_with_message() {
		TaskException taskException = assertThrows(TaskException.class, tasks::finishTask);
		
		assertEquals("No active task.", taskException.getMessage());
	}

	@Test
	void finishing_task_tells_task_writer_to_write_file() throws IOException {
		tasks.startTask(existingID(1), false);

		Mockito.reset(writer);

		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/archive.txt")).thenReturn(archiveStream);

		Task task = tasks.finishTask();

		Mockito.verify(writer).writeTask(task, archiveStream);
	}

	@Test
	void finishing_task_removes_existing_task_file() throws IOException {
		tasks.startTask(existingID(1), false);

		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/archive.txt")).thenReturn(archiveStream);

		tasks.finishTask();

		Mockito.verify(osInterface).removeFile("git-data/tasks/default/1.txt");
	}

	@Test
	void finishing_task_tells_git_control_to_add_file_and_commit() throws IOException {
		tasks.startTask(existingID(2), false);

		Mockito.reset(osInterface);

		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/archive.txt")).thenReturn(archiveStream);

		tasks.finishTask();

		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).gitCommit("Finished task 2 - 'Testing tasks 2'");
	}

	@Test
	void finishing_a_task_records_the_stop_time() {
		tasks.addTask("Test 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(existingID(1), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task task = tasks.finishTask();

		assertEquals(1000, task.addTime);
		assertEquals(4567, task.finishTime);
		assertThat(task.startStopTimes).containsOnly(
				new TaskTimes(1234, 4567)
		);
	}

	@Test
	void finish_respects_the_multiple_starts_and_stops_before_it() {
		tasks.addTask("Test 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(existingID(1), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(2345L);

		Task stop = tasks.stopTask();

		Mockito.when(osInterface.currentSeconds()).thenReturn(3456L);

		tasks.startTask(existingID(1), false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task finishTask = tasks.finishTask();

		assertEquals(1000, stop.addTime);
		assertThat(stop.startStopTimes).containsOnly(
				new TaskTimes(1234, 2345)
		);

		assertEquals(1000, finishTask.addTime);
		assertEquals(4567, finishTask.finishTime);
		assertThat(finishTask.startStopTimes).containsOnly(
				new TaskTimes(1234, 2345),
				new TaskTimes(3456, 4567)
		);
	}
}
