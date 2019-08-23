// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskList_toString_Test extends TaskBaseTestCase {
	@Test
	void to_string() {
		TaskList list = new TaskList("Test", osInterface);
		list.addTask(1, "Do Something", writer);

		assertEquals("TaskList{name='Test', fullPath='Test', tasks=[Task{id=1, task='Do Something', state=Inactive, taskTimes=[1000], recurring=false, project='', feature=''}]}", list.toString());
	}
}
