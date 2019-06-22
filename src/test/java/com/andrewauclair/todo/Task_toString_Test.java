// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Task_toString_Test {
	@Test
	void task_description_has_number_and_title() {
		assertEquals("1 - \"Test\"", new Task(1, "Test").description());
	}
	@Test
	void task_toString_displays_number_and_title() {
		Task task = new Task(1, "Test", Task.TaskState.Active,
				Arrays.asList(
					new TaskTimes(1234, 2345),
					new TaskTimes(3456, 5555),
					new TaskTimes(8473)
				)
		);
		assertEquals("Task{id=1, task='Test', state=Active, taskTimes=[1234 - 2345, 3456 - 5555, 8473]}", task.toString());
	}
}
