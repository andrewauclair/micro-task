// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskList_toString_Test extends TaskBaseTestCase {
	@Test
	void to_string() {
		TaskList list = new TaskList("Test", new TaskGroup("/"), osInterface, writer, TaskContainerState.InProgress, "none");
		Task task = newTask(newID(1), idValidator, "Do Something", TaskState.Active, 1000, Collections.singletonList(new TaskTimes(2000))).build();
		list.addTask(task);

		assertEquals("TaskList{name='Test', fullPath='/Test', state=InProgress, timeCategory='none', tasks=[Task{id=1, existingID=ExistingID{id=1}, task='Do Something', state=Active, addTime=1000, finishTime=None, startStopTimes=[2000, project='', feature=''], recurring=false, due=605800, tags=[]}]}", list.toString());
	}
}
