// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Rename_Test extends CommandsBaseTestCase {
	@Test
	void renaming_a_task() {
		tasks.addTask("Testing the rename of a task");

		commands.execute(printStream, "rename --task 1 -n \"This is the new name of the task\"");

		assertOutput(
				"Renamed task 1 - 'This is the new name of the task'",
				""
		);
	}

	@Test
	void renaming_a_task_ignores_extra_whitespace() {
		tasks.addTask("Testing the rename of a task");

		commands.execute(printStream, "rename -t 1 --name \"This is the new name of the task\"      ");

		assertOutput(
				"Renamed task 1 - 'This is the new name of the task'",
				""
		);
	}

	@Test
	void renaming_a_list() {
		tasks.addList("test");
		
		commands.execute(printStream, "rename --list test -n \"new-name\"");
		
		assertThat(tasks.getListNames()).containsOnly("/default", "/new-name");

		assertOutput(
				"Renamed list '/test' to '/new-name'",
				""
		);
	}

	@Test
	void list_renames_are_always_relative() {
		tasks.addList("/test/one");
		tasks.addList("/test/new/two");
		tasks.switchGroup("/test");
		
		commands.execute(printStream, "rename --list /test/one -n \"two\"");
		
		commands.execute(printStream, "rename --list one -n \"/test/two\"");
		
		commands.execute(printStream, "rename --list new/two -n \"three\"");
		
		commands.execute(printStream, "rename --list two -n \"new/three\"");
		
		assertThat(tasks.getListNames()).containsOnly("/default", "/test/one", "/test/new/two");
		
		assertOutput(
				"Lists must be renamed with name, not paths.",
				"",
				"Lists must be renamed with name, not paths.",
				"",
				"Lists must be renamed with name, not paths.",
				"",
				"Lists must be renamed with name, not paths.",
				""
		);
	}
	
	@Test
	void rename_task_prints_invalid_command_when_no_parameters_are_provided() {
		commands.execute(printStream, "rename");

		assertOutput(
				"Invalid command.",
				""
		);
	}
}
