// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Finish_Task_Test extends CommandsBaseTestCase {
	@Test
	void finish_active_task() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		setTime(1561078202);
		tasks.startTask(existingID(2), false);
		setTime(1561079202);
		commands.execute(printStream, "finish --active-task");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in: 16m 40s",
				""
		);

		Task task = tasks.getTask(existingID(2));

		Assertions.assertEquals(TaskState.Finished, task.state);
	}

	@Test
	void providing_a_task_id_allows_user_to_finish_specific_task() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");

		tasks.startTask(existingID(1), false);

		commands.execute(printStream, "finish --task 2");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in:  0s",
				""
		);

		Task task = tasks.getTask(existingID(1));

		assertEquals(TaskState.Active, task.state);

		task = tasks.getTask(existingID(2));

		assertEquals(TaskState.Finished, task.state);
	}

	@Test
	void finish_multiple_tasks_at_once() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		commands.execute(printStream, "finish --task 1,2,3");

		assertOutput(
				"Finished task 1 - 'Test 1'",
				"",
				"Task finished in:  0s",
				"",
				"Finished task 2 - 'Test 2'",
				"",
				"Task finished in:  0s",
				"",
				"Finished task 3 - 'Test 3'",
				"",
				"Task finished in:  0s",
				""
		);

		assertEquals(TaskState.Finished, tasks.getTask(existingID(1)).state);
		assertEquals(TaskState.Finished, tasks.getTask(existingID(2)).state);
		assertEquals(TaskState.Finished, tasks.getTask(existingID(3)).state);
	}
}
