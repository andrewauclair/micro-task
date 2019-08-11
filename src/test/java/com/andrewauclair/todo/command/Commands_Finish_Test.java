// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import org.junit.jupiter.api.Assertions;
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
		tasks.startTask(2, false);
		setTime(1561079202);
		commands.execute(printStream, "finish");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in: 16m 40s",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(2);

		assertThat(optionalTask).isPresent();
		
		optionalTask.ifPresent(task -> Assertions.assertEquals(TaskState.Finished, task.state));
	}

	@Test
	void providing_a_task_id_allows_user_to_finish_specific_task() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");

		tasks.startTask(1, false);
		
		commands.execute(printStream, "finish 2");

		assertOutput(
				"Finished task 2 - 'Task 2'",
				"",
				"Task finished in: 00s",
				""
		);

		Optional<Task> optionalTask = tasks.getTask(1);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(TaskState.Active, task.state));

		optionalTask = tasks.getTask(2);

		assertThat(optionalTask).isPresent();

		optionalTask.ifPresent(task -> assertEquals(TaskState.Finished, task.state));
	}
}