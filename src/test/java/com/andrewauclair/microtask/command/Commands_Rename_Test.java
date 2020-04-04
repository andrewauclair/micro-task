// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/new-name");

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

		assertThat(tasks.getInProgressListNames()).containsOnly("/default", "/test/one", "/test/new/two");

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
	void rename_a_group() {
		tasks.addGroup("/one/");

		assertTrue(tasks.hasGroupPath("/one/"));

		commands.execute(printStream, "rename --group /one/ -n \"/two/\"");

		assertFalse(tasks.hasGroupPath("/one/"));
		assertTrue(tasks.hasGroupPath("/two/"));

		assertOutput(
				"Renamed group '/one/' to '/two/'",
				""
		);
	}

	@Test
	void renaming_active_group_sets_active_group_to_new_group_name() {
		tasks.addGroup("/one/");
		tasks.switchGroup("/one/");

		commands.execute(printStream, "rename --group /one/ -n \"/two/\"");

		assertEquals("/two/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void renaming_parent_adds_child_list_to_new_parent() {
		tasks.addList("/one/test", true);

		tasks.setActiveList("/one/test");
		tasks.addTask("Test");

		commands.execute(printStream, "rename --group /one/ -n \"/two/\"");

		assertEquals("/two/test", tasks.getListForTask(1).getFullPath());
	}

	@Test
	void renaming_parent_adds_child_group_to_new_parent() {
		tasks.addList("/one/two/three", true);

		tasks.setActiveList("/one/two/three");
		tasks.addTask("Test");

		commands.execute(printStream, "rename --group /one/ -n \"/test/\"");

		assertEquals("/test/two/three", tasks.getListForTask(1).getFullPath());
	}

	@Test
	void rename_group__old_group_name_should_end_in_slash() {
		tasks.addGroup("/one/");

		assertTrue(tasks.hasGroupPath("/one/"));

		commands.execute(printStream, "rename --group /one -n \"/two/\"");

		assertOutput(
				"Old group name should end with /",
				""
		);
	}

	@Test
	void rename_group__new_group_name_should_end_in_slash() {
		tasks.addGroup("/one/");

		assertTrue(tasks.hasGroupPath("/one/"));

		commands.execute(printStream, "rename --group /one/ -n \"/two\"");

		assertOutput(
				"New group name should end with /",
				""
		);
	}

	@Test
	void rename_requires_name_parameter() {
		commands.execute(printStream, "rename --task 2 \"Test\"");

		assertOutput(
				"Missing required option '--name=<name>'",
				""
		);
	}

	@Test
	void rename_task_prints_invalid_command_when_no_parameters_are_provided() {
		commands.execute(printStream, "rename");

		assertOutput(
				"Missing required option '--name=<name>'",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void rename_command_help(String parameter) {
		commands.execute(printStream, "rename " + parameter);

		assertOutput(
				"Usage:  rename (-l=<list> | -g=<group> | -t=<id>) [-h] -n=<name>",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>",
				"  -n, --name=<name>",
				"  -t, --task=<id>"
		);
	}
}
