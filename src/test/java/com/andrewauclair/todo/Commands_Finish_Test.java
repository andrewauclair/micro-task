// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Finish_Test extends CommandsBaseTestCase {
	@Test
	void execute_finish_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		setTime(1561078202);
		tasks.startTask(2);
		setTime(1561079202);
		commands.execute("finish");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in: 00h 16m 40s",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(2);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(Task.TaskState.Finished, task.state));
	}
}
