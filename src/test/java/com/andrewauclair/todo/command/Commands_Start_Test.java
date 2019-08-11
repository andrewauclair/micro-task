// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test extends CommandsBaseTestCase {
	@Test
	void execute_start_command() {
		tasks.addTask("Task 1");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "start 1");

		assertOutput(
				"Started task 1 - 'Task 1'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> Assertions.assertEquals(TaskState.Active, task.state));
	}

	@Test
	void multiple_starts_prints_the_correct_start_time() {
		tasks.addTask("Task 1");

		tasks.startTask(1, false);
		Task stopTask = tasks.stopTask();

		assertThat(stopTask.getTimes()).containsOnly(
				new TaskTimes(1000),
				new TaskTimes(2000, 3000)
		);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "start 1");

		assertOutput(
				"Started task 1 - 'Task 1'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(TaskState.Active, task.state));
	}

	@Test
	void starting_second_task_stops_active_task() {
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "start 2");


		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(1234), new TaskTimes(1234, 1561078202L))),
				new Task(2, "Test 2", TaskState.Active, Arrays.asList(new TaskTimes(1234), new TaskTimes(1561078202L)))
		);

		assertOutput(
				"Stopped task 1 - 'Test 1'",
				"",
				"Started task 2 - 'Test 2'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"--finish", "-f"})
	void finish_active_task_with_finish_option(String finish) {
		Mockito.when(osInterface.currentSeconds()).thenReturn(1234L);

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		tasks.startTask(1, false);

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "start 2 " + finish);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test 1", TaskState.Finished, Arrays.asList(new TaskTimes(1234), new TaskTimes(1234, 1561078202L))),
				new Task(2, "Test 2", TaskState.Active, Arrays.asList(new TaskTimes(1234), new TaskTimes(1561078202L)))
		);

		assertOutput(
				"Finished task 1 - 'Test 1'",
				"",
				"Started task 2 - 'Test 2'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);
	}

	@Test
	void start_new_task_when_active_task_is_on_nested_list() {
		tasks.addTask("Test 1");

		tasks.addList("/one/two");
		tasks.setCurrentList("/one/two");

		tasks.addTask("Test 2");

		tasks.startTask(2, false);

		tasks.setCurrentList("/default");

		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "start 1");

		assertOutput(
				"Stopped task 2 - 'Test 2'",
				"",
				"Started task 1 - 'Test 1'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);
	}
}