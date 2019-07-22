// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test extends CommandsBaseTestCase {
	@Test
	void execute_start_command() {
		tasks.addTask("Task 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);
		
		commands.execute(printStream, "start 1");

		assertEquals("Started task 1 - 'Task 1'" + Utils.NL + Utils.NL +
				"06/20/2019 07:50:02 PM -" + Utils.NL + Utils.NL, outputStream.toString());

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(TaskState.Active, task.state));
	}

	@Test
	void multiple_starts_prints_the_correct_start_time() {
		tasks.addTask("Task 1");

		tasks.startTask(1);
		Task stopTask = tasks.stopTask();

		assertThat(stopTask.getTimes()).hasSize(1);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);
		
		commands.execute(printStream, "start 1");

		assertEquals("Started task 1 - 'Task 1'" + Utils.NL + Utils.NL +
				"06/20/2019 07:50:02 PM -" + Utils.NL + Utils.NL, outputStream.toString());

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(TaskState.Active, task.state));
	}

	@Test
	void starting_second_task_stops_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);
		
		commands.execute(printStream, "start 2");

		assertThat(tasks.getTasks().stream()
				.filter(task -> task.state == TaskState.Active)).hasSize(1);

		assertOutput(
				"Stopped task 1 - 'Test 1'",
				"",
				"Started task 2 - 'Test 2'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);
	}
}
