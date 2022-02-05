// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Stop_Task_Test extends CommandsBaseTestCase {
	@Test
	void execute_stop_command() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(existingID(1), false);
		setTime(1561079202);
		commands.execute(printStream, "stop task");

		assertOutput(
				"Stopped task 1 - 'Task 1'",
				"",
				"06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM",
				"",
				"Task was active for: 16m 40s",
				""
		);

		Task task = tasks.getTask(existingID(1));

		Assertions.assertEquals(TaskState.Inactive, task.state);
	}

	@Test
	void multiple_starts_and_stops_only_shows_the_latest_time() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(existingID(1), false);
		setTime(1561079202);
		commands.execute(printStream, "stop task");
		setTime(1561080202);
		tasks.startTask(existingID(1), false);
		setTime(1561081202);
		outputStream.reset();

		commands.execute(printStream, "stop task");

		assertOutput(
				"Stopped task 1 - 'Task 1'",
				"",
				"06/20/2019 08:23:22 PM - 06/20/2019 08:40:02 PM",
				"",
				"Task was active for: 16m 40s",
				""
		);

		Task task = tasks.getTask(existingID(1));

		assertEquals(TaskState.Inactive, task.state);
	}

	@Test
	void stop_command_help() {
		commands.execute(printStream, "stop --help");

		assertOutput(
				"Usage:  stop [-h] [COMMAND]",
						"Stop the active task, list, group, project, feature or tags.",
						"  -h, --help   Show this help message.",
						"Commands:",
						"  task",
						"  list",
						"  group",
						"  project",
						"  feature",
						"  milestone",
						"  tags"
		);
	}
}
