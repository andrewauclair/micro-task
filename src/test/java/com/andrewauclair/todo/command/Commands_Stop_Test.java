// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Stop_Test extends CommandsBaseTestCase {
	@Test
	void execute_stop_command() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1, false);
		setTime(1561079202);
		commands.execute(printStream, "stop");

		assertOutput(
				"Stopped task 1 - 'Task 1'",
				"",
				"06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM",
				"",
				"Task was active for: 16m 40s",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> Assertions.assertEquals(TaskState.Inactive, task.state));
	}

	@Test
	void multiple_starts_and_stops_only_shows_the_latest_time() {
		tasks.addTask("Task 1");
		setTime(1561078202);
		tasks.startTask(1, false);
		setTime(1561079202);
		commands.execute(printStream, "stop");
		setTime(1561080202);
		tasks.startTask(1, false);
		setTime(1561081202);
		outputStream.reset();

		commands.execute(printStream, "stop");

		assertOutput(
				"Stopped task 1 - 'Task 1'",
				"",
				"06/20/2019 08:23:22 PM - 06/20/2019 08:40:02 PM",
				"",
				"Task was active for: 16m 40s",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(TaskState.Inactive, task.state));
	}
}
