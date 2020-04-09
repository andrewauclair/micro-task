// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskBuilderTest {
	@Test
	void finishing_task_with_no_times_results_in_finish_time() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		
		TaskBuilder builder = new TaskBuilder(task);
		
		Task finish = builder.finish(1234);
		
		assertThat(finish.getAllTimes().get(0)).isEqualTo(new TaskTimes(0));
		assertThat(finish.getStartStopTimes()).isEmpty();
		assertThat(finish.getAllTimes().get(1)).isEqualTo(new TaskTimes(1234));
	}
	
	@Test
	void does_not_remove_last_time_when_not_finished() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		
		Task finalTask = new TaskBuilder(task)
				.withState(TaskState.Finished)
				.build();
		
		assertThat(finalTask.getAllTimes()).containsOnly(new TaskTimes(0));
	}
	
	@Test
	void does_not_remove_last_time_when_state_does_not_change() {
		Task task = new Task(1, "Test", TaskState.Finished, Collections.singletonList(new TaskTimes(0)));
		
		Task finalTask = new TaskBuilder(task)
				.withState(TaskState.Finished)
				.build();
		
		assertThat(finalTask.getAllTimes()).containsOnly(new TaskTimes(0));
	}

	@Test
	void build_task_starting_with_only_id() {
		Task task = new TaskBuilder(1)
				.withName("Test")
				.withState(TaskState.Finished)
				.withTime(new TaskTimes(123))
				.withTime(new TaskTimes(124, 224))
				.build();

		Task expectedTask = new Task(1, "Test", TaskState.Finished, Arrays.asList(new TaskTimes(123), new TaskTimes(124, 224)));

		assertEquals(expectedTask, task);
	}

	@Test
	void finished_task_can_not_be_renamed() {
		TaskBuilder builder = new TaskBuilder(1)
				.withName("Test")
				.withState(TaskState.Finished);

		TaskException taskException = assertThrows(TaskException.class, () -> builder.withName("Test 2"));

		assertEquals("Task 1 cannot be renamed because it has been finished.", taskException.getMessage());
	}
}
