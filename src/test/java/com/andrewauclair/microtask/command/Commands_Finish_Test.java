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
	void finish_a_group() {
		tasks.addGroup("/test/");
		tasks.addList("/test/one", true);
		tasks.addTask("Test", "/test/one");

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Finished group '/test/'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getGroup("/test/").getState());
		assertNotNull(tasks.getTask(1));
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void finish_command_help(String parameter) {
		commands.execute(printStream, "finish " + parameter);

		assertOutput(
				"Usage:  finish (-t=<id> | -l=<list> | -g=<group> | -a) [-h]",
				"  -a, --active",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -t, --task=<id>"
		);
	}
}
