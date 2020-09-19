// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskList_toString_Test extends TaskBaseTestCase {
	@Test
	void to_string() {
		TaskList list = new TaskList("Test", new TaskGroup("/"), osInterface, writer, TaskContainerState.InProgress);
		list.addTask(newID(1), "Do Something");

		assertEquals("TaskList{name='Test', fullPath='/Test', tasks=[Task{id=1, task='Do Something', state=Inactive, taskTimes=[1000, project='', feature=''], recurring=false, tags=[]}]}", list.toString());
	}
}
