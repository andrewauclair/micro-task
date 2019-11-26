// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_ActiveTask_Finish_Test extends TaskBaseTestCase {
	@BeforeEach
	void setup() throws IOException {
		super.setup();
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
	}

	@Test
	void finishing_a_task_removes_it_from_the_task_list() {
		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Testing tasks", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				new Task(2, "Testing tasks 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(2000)))
		);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(2, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task task = tasks.finishTask();

		Task finishedTask = new Task(2, "Testing tasks 2", TaskState.Finished, Arrays.asList(new TaskTimes(2000), new TaskTimes(1234, 4567)));

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Testing tasks", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				finishedTask
		);

		assertEquals(finishedTask, task);
	}

	@Test
	void finishing_a_task_resets_the_active_task() {
		Task oldTask = tasks.startTask(1, false);
		Task finishedTask = tasks.finishTask();

		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::getActiveTask);

		assertEquals("No active task.", runtimeException.getMessage());
		assertThat(tasks.getTasks()).doesNotContain(oldTask);
		assertThat(tasks.getTasks()).contains(finishedTask);
	}

	@Test
	void finish_with_no_active_task_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, tasks::finishTask);

		assertEquals("No active task.", runtimeException.getMessage());
	}

	@Test
	void finishing_task_tells_task_writer_to_write_file() {
		tasks.startTask(1, false);

		Mockito.reset(writer);

		Task task = tasks.finishTask();

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}

	@Test
	void finishing_task_tells_git_control_to_add_file_and_commit() {
		tasks.startTask(2, false);

		Mockito.reset(osInterface);

		tasks.finishTask();

		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/2.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Finished task 2 - 'Testing tasks 2'\"", false);
	}

	@Test
	void finishing_a_task_records_the_stop_time() {
		tasks.addTask("Test 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task task = tasks.finishTask();

		assertThat(task.getTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, 4567)
		);
	}

	@Test
	void finish_respects_the_multiple_starts_and_stops_before_it() {
		tasks.addTask("Test 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(2345L);

		Task stop = tasks.stopTask();

		Mockito.when(osInterface.currentSeconds()).thenReturn(3456L);

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(4567L);

		Task finishTask = tasks.finishTask();

		assertThat(stop.getTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, 2345)
		);

		assertThat(finishTask.getTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(1234, 2345),
				new TaskTimes(3456, 4567)
		);
	}
}
