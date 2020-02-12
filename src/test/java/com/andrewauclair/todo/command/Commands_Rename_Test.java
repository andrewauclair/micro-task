// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
		tasks.addList("test", true);

		commands.execute(printStream, "rename --list test -n \"new-name\"");

		assertThat(tasks.getListNames()).containsOnly("/default", "/new-name");

		assertOutput(
				"Renamed list '/test' to '/new-name'",
				""
		);
	}

	@Test
	void list_renames_are_always_relative() {
		tasks.addList("/test/one", true);
		tasks.addList("/test/new/two", true);
		tasks.switchGroup("/test/");

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
	void rename_requires_name_parameter() {
		commands.execute(printStream, "rename --task 2 \"Test\"");

		assertOutput(
				"Unmatched argument at index 3: 'Test'",
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

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void rename_command_help(String parameter) {
		commands.execute(printStream, "rename " + parameter);

		assertOutput(
				"Usage:  rename [-h] [-l=<list>] [-n=<name>] [-t=<id>]",
				"  -h, --help          Show this help message.",
				"  -l, --list=<list>",
				"  -n, --name=<name>",
				"  -t, --task=<id>"
		);
	}
}
