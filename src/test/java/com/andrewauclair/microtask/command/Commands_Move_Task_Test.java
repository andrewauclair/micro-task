// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Move_Task_Test extends CommandsBaseTestCase {
	@Test
	void move_task_to_a_new_list() {
		tasks.addTask("Test 1");

		tasks.addList("one", true);

		commands.execute(printStream, "move --task 1 --dest-list one");

		assertThat(tasks.getTasksForList("/one")).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);

		assertOutput(
				"Moved task 1 to list '/one'",
				""
		);
	}

	@Test
	void move_task_between_lists_in_different_groups() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList(existingList("/one/two/three"));

		tasks.addTask("Test 1");

		commands.execute(printStream, "move --task 1 --dest-list /one/test/five");

		assertThat(tasks.getTasksForList("/one/test/five")).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);

		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_multiple_tasks_at_once() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList(existingList("/one/two/three"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		commands.execute(printStream, "move --task 1,2,3 --dest-list /one/test/five");

		assertThat(tasks.getTasksForList("/one/test/five")).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000))),
				new Task(2, "Test 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(2000))),
				new Task(3, "Test 3", TaskState.Inactive, Collections.singletonList(new TaskTimes(3000)))
		);

		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				"Moved task 2 to list '/one/test/five'",
				"Moved task 3 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_task_must_have_dest_list_not_list_option() {
		tasks.addTask("Test");
		tasks.addList("/dest", true);

		commands.execute(printStream, "move --task 1 --list /dest");

		assertOutput(
				"Error: --task=<id>, --list=<list> are mutually exclusive (specify only one)",
				""
		);
	}

	@Test
	void move_task_requires_dest_list() {
		commands.execute(printStream, "move --task 1");

		assertOutput(
				"move --task requires --dest-list",
				""
		);
	}
}
