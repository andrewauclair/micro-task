// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Task_toString_Test {
	@Test
	void task_description_has_number_and_title() {
		assertEquals("1 - 'Test'", new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0))).description());
	}

	@Test
	void task_toString_displays_number_and_title() {
		Task task = new Task(1, "Test", TaskState.Active,
				Arrays.asList(
						new TaskTimes(1234, 2345),
						new TaskTimes(3456, 5555),
						new TaskTimes(8473, "Project", "Feature")
				),
				true
		);
		assertEquals("Task{id=1, task='Test', state=Active, taskTimes=[1234 - 2345, project='', feature='', 3456 - 5555, project='', feature='', 8473, project='Project', feature='Feature'], recurring=true}", task.toString());
	}
}
