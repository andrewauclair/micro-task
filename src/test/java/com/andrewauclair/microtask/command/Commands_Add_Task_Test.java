// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
				new TaskBuilder(1)
						.withTask("Task 1")
						.withState(TaskState.Inactive)
						.withAddTime(1000)
						.withDueTime(1000 + Tasks.DEFAULT_DUE_TIME)
						.build()
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
				new TaskBuilder(1)
						.withTask("Test")
						.withState(TaskState.Inactive)
						.withAddTime(1000)
						.withRecurring(true)
						.withDueTime(1000 + Tasks.DEFAULT_DUE_TIME)
						.build()
		);
	}

	@Test
	void add_task_with_2_week_due_time() {
		osInterface.setIncrementTime(false);

		commands.execute(printStream, "add task \"Test\" --due p2w");

		assertOutput(
				"Added task 1 - 'Test'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new TaskBuilder(1)
						.withTask("Test")
						.withState(TaskState.Inactive)
						.withAddTime(1000)
						.withDueTime(1000 + (604_800L * 2))
						.build()
		);
	}

	@Test
	void add_task_with_due_date_of_today() {
		osInterface.setTime(50000);
		osInterface.setIncrementTime(false);

		commands.execute(printStream, "add task \"Test\" --due-today");

		assertOutput(
				"Added task 1 - 'Test'",
				""
		);

		assertThat(tasks.getTasks()).containsOnly(
				new TaskBuilder(1)
						.withTask("Test")
						.withState(TaskState.Inactive)
						.withAddTime(50000)
						.withDueTime(50000)
						.build()
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
		assertThat(task.tags).containsOnly("design", "phase-1");
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
				new TaskBuilder(1)
						.withTask("Test")
						.withState(TaskState.Inactive)
						.withAddTime(1000)
						.withDueTime(1000 + Tasks.DEFAULT_DUE_TIME)
						.build()
//				new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), false, 2000 + Tasks.DEFAULT_DUE_TIME, Collections.emptyList())
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
	void default_due_date_is_1_week() {
		Mockito.when(osInterface.currentSeconds()).thenReturn(1561078202L);

		commands.execute(printStream, "add task \"Test\"");

		assertEquals(1561078202L + 604_800L, tasks.getTask(existingID(1)).dueTime);
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
