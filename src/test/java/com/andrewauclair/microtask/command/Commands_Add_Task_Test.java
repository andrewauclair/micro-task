// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Add_Task_Test extends CommandsBaseTestCase {
	@Test
	void execute_add_command() {
		commands.execute(printStream, "add task \"Task 1\"");

		assertOutput(
				"Added task 1 - 'Task 1'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Task 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), false, Collections.emptyList())
		);
	}

	@Test
	void add_recurring_task() {
		commands.execute(printStream, "add task \"Test\" --recurring");

		assertOutput(
				"Added task 1 - 'Test'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), true, Collections.emptyList())
		);
	}

	@Test
	void add_task_with_tags() {
		commands.execute(printStream, "add task \"Test\" --tags design,phase-1");

		assertOutput(
				"Added task 1 - 'Test'",
				"with tag(s): design, phase-1",
				""
		);

		Task task = tasks.getTask(existingID(1));
		assertThat(task.getTags()).containsOnly("design", "phase-1");
	}

	@Test
	void add_task_to_specific_list() {
		tasks.addList(newList("one"), true);
		commands.execute(printStream, "add task \"Test\" --list one");

		assertOutput(
				"Added task 1 - 'Test'",
				"to list '/one'",
				""
		);

		assertThat(tasks.getTasksForList(existingList("one"))).containsOnly(
				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), false, Collections.emptyList())
		);
	}

	@Test
	void add_task_to_a_list_in_a_group() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		tasks.setCurrentList(existingList("/test/one"));

		commands.execute(printStream, "add task \"Test 1\"");

		assertOutput(
				"Added task 1 - 'Test 1'",
				""
		);
	}

	@Test
	void start_task_when_adding_it() {
		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "add task \"Test\" -s");

		assertEquals(TaskState.Active, tasks.getTask(existingID(1)).state);

		assertOutput(
				"Added task 1 - 'Test'",
				"",
				"Started task 1 - 'Test'",
				"",
				"06/20/2019 07:50:02 PM -",
				""
		);
	}

	@Test
	void add_command_help() {
		commands.execute(printStream, "add --help");

		assertOutput(
				"Usage:  add [-h] COMMAND",
						"Add a task, list, group, project, feature or milestone.",
						"  -h, --help   Show this help message.",
						"Commands:",
						"  task",
						"  list",
						"  group",
				        "  project",
						"  feature",
						"  milestone"
		);
	}
}
