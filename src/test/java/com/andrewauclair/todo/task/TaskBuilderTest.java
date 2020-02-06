// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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
}
