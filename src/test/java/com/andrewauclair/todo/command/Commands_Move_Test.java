// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Move_Test extends CommandsBaseTestCase {
	@Test
	void execute_move_command() {
		tasks.addTask("Test 1");
		
		tasks.addList("one", true);
		
		commands.execute(printStream, "move --task 1 --dest-list one");
		
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
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList("/one/two/three");
		
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
		tasks.setActiveList("/one/two/three");
		
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
	void move_list_from_one_group_to_another() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList("/one/two/three");
		
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "move --list /one/two/three --dest-group /one/test/");
		
		assertFalse(tasks.hasListWithName("/one/two/three"));
		assertTrue(tasks.hasListWithName("/one/test/three"));
		
		assertOutput(
				"Moved list /one/two/three to group '/one/test/'",
				""
		);
	}
	
	@Test
	void move_list_to_root_group() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/test/five", true);
		tasks.setActiveList("/one/two/three");
		
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "move --list /one/two/three --dest-group /");
		
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
		
		commands.execute(printStream, "move --group /one/ --dest-group /two/");
		
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
		
		commands.execute(printStream, "move --group /one/two/ --dest-group /");
		
		assertFalse(tasks.hasGroupPath("/one/two/"));
		assertTrue(tasks.hasGroupPath("/two/"));
		
		assertOutput(
				"Moved group '/one/two/' to group '/'",
				""
		);
	}
	
	@Test
	void move_task_must_have_dest_list_not_list_option() {
		tasks.addTask("Test");
		tasks.addList("/dest", true);
		
		commands.execute(printStream, "move --task 1 --list /dest");
		
		assertOutput(
				"move --task requires --dest-list",
				""
		);
	}
	
	@Test
	void move_list_requires_dest_group() {
		commands.execute(printStream, "move --list /default --group /one/");
		
		assertOutput(
				"move --list requires --dest-group",
				""
		);
	}
	
	@Test
	void move_group_rquires_dest_group() {
		commands.execute(printStream, "move --group /one/");
		
		assertOutput(
				"move --group requires --dest-group",
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void move_command_help(String parameter) {
		commands.execute(printStream, "move " + parameter);
		
		assertOutput(
				"Usage:  move [-h] [--dest-group=<dest_group>] [--dest-list=<dest_list>]",
				"             [-g=<group>] [-l=<list>] [-t=<id>[,<id>...]]...",
				"      --dest-group=<dest_group>",
				"",
				"      --dest-list=<dest_list>",
				"",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -t, --task=<id>[,<id>...]",
				""
		);
	}
}
