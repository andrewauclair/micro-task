// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void execute_move_command() {
		tasks.addTask("Test 1");

		tasks.addList("one");

		commands.execute(printStream, "move --task 1 one");

		assertThat(tasks.getTasksForList("/one")).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);

		assertOutput(
				"Moved task 1 to list 'one'",
				""
		);
	}
	
	@Test
	void move_task_between_lists_in_different_groups() {
		tasks.addList("/one/two/three");
		tasks.addList("/one/test/five");
		tasks.setActiveList("/one/two/three");
		
		tasks.addTask("Test 1");

		commands.execute(printStream, "move --task 1 /one/test/five");

		assertThat(tasks.getTasksForList("/one/test/five")).containsOnly(
				new Task(1, "Test 1", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)))
		);
		
		assertOutput(
				"Moved task 1 to list '/one/test/five'",
				""
		);
	}

	@Test
	void move_list_from_one_group_to_another() {
		tasks.addList("/one/two/three");
		tasks.addList("/one/test/five");
		tasks.setActiveList("/one/two/three");

		tasks.addTask("Test 1");
		
		commands.execute(printStream, "move --list /one/two/three /one/test/");

		assertFalse(tasks.hasListWithName("/one/two/three"));
		assertTrue(tasks.hasListWithName("/one/test/three"));

		assertOutput(
				"Moved list /one/two/three to group '/one/test/'",
				""
		);
	}

	@Test
	void move_list_to_root_group() {
		tasks.addList("/one/two/three");
		tasks.addList("/one/test/five");
		tasks.setActiveList("/one/two/three");

		tasks.addTask("Test 1");

		commands.execute(printStream, "move --list /one/two/three /");

		assertFalse(tasks.hasListWithName("/one/two/three"));
		assertTrue(tasks.hasListWithName("/three"));

		assertOutput(
				"Moved list /one/two/three to group '/'",
				""
		);
	}

	@Test
	void move_group_from_one_group_to_another() {
		tasks.createGroup("/one/");
		tasks.createGroup("/two/");
		
		commands.execute(printStream, "move --group /one/ /two/");
		
		assertFalse(tasks.hasGroupPath("/one/"));
		assertTrue(tasks.hasGroupPath("/two/one/"));

		assertOutput(
				"Moved group '/one/' to group '/two/'",
				""
		);
	}

	@Test
	void move_group_to_root_group() {
		tasks.createGroup("/one/two/");
		
		commands.execute(printStream, "move --group /one/two/ /");
		
		assertFalse(tasks.hasGroupPath("/one/two/"));
		assertTrue(tasks.hasGroupPath("/two/"));

		assertOutput(
				"Moved group '/one/two/' to group '/'",
				""
		);
	}
}
