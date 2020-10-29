// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskBuilderTest {
	@Test
	void finishing_task_with_no_times_results_in_finish_time() {
		Task task = newTask(1, "Test", TaskState.Inactive, 0);
		
		TaskBuilder builder = new TaskBuilder(task);
		
		Task finish = builder.finish(1234);

		assertEquals(0, finish.addTime);
		//		// exclude add and finish when finished
//		if (state == TaskState.Finished) {
//			return startStopTimes.subList(1, startStopTimes.size() - 1);
//		}
//		// exclude add
//		return startStopTimes.subList(1, startStopTimes.size());
		assertThat(finish.startStopTimes).isEmpty();
		assertEquals(1234, finish.finishTime);
	}
	
	@Test
	void does_not_remove_last_time_when_not_finished() {
		Task task = newTask(1, "Test", TaskState.Inactive, 0);
		
		Task finalTask = new TaskBuilder(task)
				.withState(TaskState.Finished)
				.build();
		
		assertThat(finalTask.startStopTimes).isEmpty();
	}
	
	@Test
	void does_not_remove_last_time_when_state_does_not_change() {
		Task task = newTask(1, "Test", TaskState.Finished, 0);
		
		Task finalTask = new TaskBuilder(task)
				.withState(TaskState.Finished)
				.build();

		assertThat(finalTask.startStopTimes).isEmpty();
	}

	@Test
	void build_task_starting_with_only_id() {
		Task task = new TaskBuilder(1)
				.withTask("Test")
				.withState(TaskState.Inactive)
				.withAddTime(123)
				.withStartStopTime(new TaskTimes(124, 224))
				.withDueTime(604923)
				.build();

		Task expectedTask = newTask(1, "Test", TaskState.Inactive, 123, Collections.singletonList(new TaskTimes(124, 224)));

		assertEquals(expectedTask, task);
	}

	@Test
	void finished_task_can_not_be_renamed() {
		TaskBuilder builder = new TaskBuilder(1)
				.withTask("Test")
				.withState(TaskState.Finished);

		TaskException taskException = assertThrows(TaskException.class, () -> builder.withTask("Test 2"));

		assertEquals("Task 1 cannot be renamed because it has been finished.", taskException.getMessage());
	}
}
