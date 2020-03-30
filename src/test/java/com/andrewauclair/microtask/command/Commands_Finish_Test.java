// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskContainerState;
import com.andrewauclair.microtask.task.TaskState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Commands_Finish_Test extends CommandsBaseTestCase {
	@Test
	void execute_finish_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		setTime(1561078202);
		tasks.startTask(2, false);
		setTime(1561079202);
		commands.execute(printStream, "finish --active");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in: 16m 40s",
				""
		);

		Task task = tasks.getTask(2);

		Assertions.assertEquals(TaskState.Finished, task.state);
	}

	@Test
	void providing_a_task_id_allows_user_to_finish_specific_task() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");

		tasks.startTask(1, false);

		commands.execute(printStream, "finish --task 2");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in:  0s",
				""
		);

		Task task = tasks.getTask(1);

		assertEquals(TaskState.Active, task.state);

		task = tasks.getTask(2);

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

		assertEquals(TaskState.Finished, tasks.getTask(1).state);
		assertEquals(TaskState.Finished, tasks.getTask(2).state);
		assertEquals(TaskState.Finished, tasks.getTask(3).state);
	}

	@Test
	void finish_a_list() {
		tasks.addList("/test", true);

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"Finished list '/test'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getListByName("/test").getState());
	}

	@Test
	void not_allowed_to_finish_active_list() {
		tasks.setActiveList("/default");

		commands.execute(printStream, "finish --list /default");

		assertOutput(
				"List to finish must not be active.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName("/default").getState());
	}

	@Test
	void lists_with_tasks_that_are_not_finished_cannot_be_finished() {
		tasks.addList("/test", true);

		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setActiveList("/default");

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"List to finish still has tasks to complete.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName("/test").getState());
	}

	@Test
	void finish_a_group() {
		tasks.addGroup("/test/");
		tasks.addList("/test/one", true);
		tasks.addTask("Test", "/test/one");
		tasks.finishTask(1);

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Finished group '/test/'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getGroup("/test/").getState());
		assertNotNull(tasks.getTask(1));
	}

	@Test
	void not_allowed_to_finish_active_group() {
		tasks.addList("/test/one", true);

		tasks.switchGroup("/test/");

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group to finish must not be active.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/").getState());
	}

	@Test
	void groups_with_tasks_that_are_not_finished_cannot_be_finished() {
		tasks.addList("/test/one", true);

		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		tasks.setActiveList("/default");

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group to finish still has tasks to complete.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName("/test/one").getState());
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void finish_command_help(String parameter) {
		commands.execute(printStream, "finish " + parameter);

		assertOutput(
				"Usage:  finish (-t=<id>[,<id>...] [-t=<id>[,<id>...]]... | -l=<list> |",
				"               -g=<group> | -a) [-h]",
				"  -a, --active",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -t, --task=<id>[,<id>...]",
				""
		);
	}
}
