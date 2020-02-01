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
		
		assertThat(finish.getStartStopTimes()).containsOnly(
				new TaskTimes(1234, 1234)
		);
	}
}
