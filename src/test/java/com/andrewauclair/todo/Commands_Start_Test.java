// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test extends CommandsBaseTestCase {
	@Test
	void execute_start_command() {
		tasks.addTask("Task 1");
		commands.execute("start 1");

		assertEquals("Started task 1 - \"Task 1\"" + Utils.NL, outputStream.toString());

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(Task.TaskState.Active, task.state));
	}

	@Test
	void starting_second_task_stops_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1);
		tasks.startTask(2);

		assertThat(tasks.getTasks().stream()
				.filter(task -> task.state == Task.TaskState.Active)).hasSize(1);
	}
}
