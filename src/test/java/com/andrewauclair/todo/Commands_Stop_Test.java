// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Stop_Test extends CommandsBaseTestCase {
	@Test
	void execute_stop_command() {
		tasks.addTask("Task 1");
		tasks.startTask(1);
		commands.execute("stop");

		assertEquals("Stopped task 1 - \"Task 1\"" + Utils.NL, outputStream.toString());

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(Task.TaskState.Inactive, task.state));
	}
}
