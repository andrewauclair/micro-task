// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_State_Task_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_task_to_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(existingID(1));

		assertEquals(TaskState.Finished, tasks.getTask(existingID(1)).state);

		commands.execute(printStream, "set task 1 --inactive");

		assertEquals(TaskState.Inactive, tasks.getTask(existingID(1)).state);

		assertOutput(
				"Set state of task 1 - 'Test' to Inactive",
				""
		);
	}

	@Test
	void write_task_when_setting_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(existingID(1));

		Mockito.reset(writer);

		commands.execute(printStream, "set task 1 --inactive");

		Mockito.verify(writer).writeTask(new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))), "git-data/tasks/default/1.txt");
	}

	@Test
	void write_git_commit_when_setting_inactive() {
		tasks.addTask("Test");
		tasks.finishTask(existingID(1));

		Mockito.reset(osInterface);

		commands.execute(printStream, "set task 1 --inactive");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Set state for task 1 to Inactive");
	}

	@Test
	void task_needs_to_be_finished_before_it_can_be_set_back_to_inactive() {
		tasks.addTask("Test");

		Mockito.reset(osInterface);

		commands.execute(printStream, "set task 1 --inactive");

		Mockito.verifyNoInteractions(osInterface);

		assertOutput(
				"Task 1 - 'Test' must be finished first",
				""
		);
	}
}
