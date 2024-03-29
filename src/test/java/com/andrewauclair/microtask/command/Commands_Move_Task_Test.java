// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.assertj.core.api.Assertions.assertThat;

class Commands_Move_Task_Test extends CommandsBaseTestCase {
	@Test
	void move_task_to_a_new_list() {
		tasks.addTask("Test 1");

		tasks.addList(newList("one"), true);

		commands.execute(printStream, "move task 1 --dest-list one");

		assertThat(tasks.getTasksForList(existingList("/one"))).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Inactive, 1000).build()
		);

		assertOutput(
				"Moved task 1 to list '/one'",
				""
		);
	}

	@Test
	void move_task_between_lists_in_different_groups() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/two/three"), true);
		tasks.addList(newList("/one/test/five"), true);
		tasks.setCurrentList(existingList("/one/two/three"));

		tasks.addTask("Test 1");

		commands.execute(printStream, "move task 1 --dest-list /one/test/five");

		assertThat(tasks.getTasksForList(existingList("/one/test/five"))).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Inactive, 1000).build()
		);

		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_multiple_tasks_at_once() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/two/three"), true);
		tasks.addList(newList("/one/test/five"), true);
		tasks.setCurrentList(existingList("/one/two/three"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		commands.execute(printStream, "move task 1,2,3 --dest-list /one/test/five");

		assertThat(tasks.getTasksForList(existingList("/one/test/five"))).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Inactive, 1000).build(),
				TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, 2000).build(),
				TestUtils.existingTask(existingID(3), "Test 3", TaskState.Inactive, 3000).build()
		);

		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				"Moved task 2 to list '/one/test/five'",
				"Moved task 3 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_multiple_tasks_at_once_interactively() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/two/three"), true);
		tasks.addList(newList("/one/test/five"), true);
		tasks.setCurrentList(existingList("/one/two/three"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		Mockito.when(osInterface.promptChoice("move task 1")).thenReturn(true);
		Mockito.when(osInterface.promptChoice("move task 2")).thenReturn(false);
		Mockito.when(osInterface.promptChoice("move task 3")).thenReturn(true);

		commands.execute(printStream, "move task 1,2,3 --dest-list /one/test/five --interactive");

		assertThat(tasks.getTasksForList(existingList("/one/test/five"))).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Inactive, 1000).build(),
				TestUtils.existingTask(existingID(3), "Test 3", TaskState.Inactive, 3000).build()
		);

		assertOutput(
				"1 - 'Test 1'",
				"Moved task 1 to list '/one/test/five'",
				"2 - 'Test 2'",
				"3 - 'Test 3'",
				"Moved task 3 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_all_tasks_from_one_list_to_another() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/two/three"), true);
		tasks.addList(newList("/one/test/five"), true);
		tasks.setCurrentList(existingList("/one/two/three"));

		TaskBuilder task180_builder = new TaskBuilder(idValidator, newID(180))
				.withTask("Test 180")
				.withDueTime(604800);

		Task task180 = tasks.addTask(task180_builder);
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");

		tasks.finishTask(existingID(4));

		commands.execute(printStream, "move task --src-list /one/two/three --dest-list /one/test/five");

		assertThat(tasks.getTasksForList(existingList("/one/test/five"))).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Inactive, 1000).build(),
				TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, 2000).build(),
				TestUtils.existingTask(existingID(3), "Test 3", TaskState.Inactive, 3000).build(),
				task180
		);

		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				"Moved task 2 to list '/one/test/five'",
				"Moved task 3 to list '/one/test/five'",
				"Moved task 180 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_all_tasks_from_one_list_to_another_interactive() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addGroup(newGroup("/one/test/"));
		tasks.addList(newList("/one/two/three"), true);
		tasks.addList(newList("/one/test/five"), true);
		tasks.setCurrentList(existingList("/one/two/three"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");

		tasks.finishTask(existingID(4));

		Mockito.when(osInterface.promptChoice("move task 1")).thenReturn(true);
		Mockito.when(osInterface.promptChoice("move task 2")).thenReturn(false);
		Mockito.when(osInterface.promptChoice("move task 3")).thenReturn(true);

		commands.execute(printStream, "move task --src-list /one/two/three --dest-list /one/test/five --interactive");

		assertThat(tasks.getTasksForList(existingList("/one/test/five"))).containsOnly(
				TestUtils.existingTask(existingID(1), "Test 1", TaskState.Inactive, 1000).build(),
				TestUtils.existingTask(existingID(3), "Test 3", TaskState.Inactive, 3000).build()
		);

		assertOutput(
				"1 - 'Test 1'",
				"Moved task 1 to list '/one/test/five'",
				"2 - 'Test 2'",
				"3 - 'Test 3'",
				"Moved task 3 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_task_requires_dest_list() {
		tasks.addTask("Test");
		commands.execute(printStream, "move task 1");

		assertOutput(
				"Missing required option: '--dest-list=<dest_list>'",
				""
		);
	}
}
