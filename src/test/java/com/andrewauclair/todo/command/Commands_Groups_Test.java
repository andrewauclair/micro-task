// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;

class Commands_Groups_Test extends CommandsBaseTestCase {
	@Test
	void create_group_command() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(listStream));

		commands.execute(printStream, "mk -g /test/one/two/three/");

		assertTrue(tasks.hasGroupPath("/test/one/two/three/"));
		
		InOrder inOrder = Mockito.inOrder(osInterface);
		
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/one/group.txt");
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/one/two/group.txt");
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/one/two/three/group.txt");
		inOrder.verify(osInterface).runGitCommand("git add .", false);
		inOrder.verify(osInterface).runGitCommand("git commit -m \"Created group '/test/one/two/three/'\"", false);

		TestUtils.assertOutput(listStream,
				"",
				"",
				"InProgress",
				""
		);

		assertOutput(
				"Created group '/test/one/two/three/'",
				""
		);
	}

	@Test
	void switch_group_command() {
		commands.execute(printStream, "mk --group /test/one/two/three/");
		commands.execute(printStream, "ch -g /test/one/two/three/");

		assertEquals("/test/one/two/three/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void create_relative_group() {
		commands.execute(printStream, "mk -g one/");
		commands.execute(printStream, "ch -g one/");

		outputStream.reset();

		commands.execute(printStream, "mk -g two/three/");

		assertTrue(tasks.hasGroupPath("/one/two/three/"));

		assertOutput(
				"Created group '/one/two/three/'",
				""
		);
	}

	@Test
	void switch_to_relative_group() {
		commands.execute(printStream, "mk -g /one/two/three/");
		commands.execute(printStream, "ch -g /one/two/");

		outputStream.reset();

		commands.execute(printStream, "ch -g three/");

		assertOutput(
				"Switched to group '/one/two/three/'",
				""
		);
	}

	@Test
	void move_back_one_group_with_dot_dot_parameter() {
		commands.execute(printStream, "mk -g /one/two/");
		commands.execute(printStream, "ch -g /one/two/");

		outputStream.reset();

		commands.execute(printStream, "ch -g ..");

		assertOutput(
				"Switched to group '/one/'",
				""
		);

		assertEquals("/one/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void dot_dot_parameter_does_nothing_in_root_group() {
		commands.execute(printStream, "ch -g ..");

		assertOutput();

		assertEquals("/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void make_command_requires_a_list_or_group() {
		commands.execute(printStream, "mk");

		assertOutput(
				"Error: Missing required argument (specify one of these): (-l=<list> | -g=<group>)",
				""
		);
	}

	@Test
	void make_command_only_takes_list_or_group_at_once() {
		commands.execute(printStream, "mk -l /one -g /two/");

		assertOutput(
				"Error: --list=<list>, --group=<group> are mutually exclusive (specify only one)",
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"ch", "finish", "list", "move", "set-group", "move", "search", "rename"})
	void change_command_group_option_has_group_completer(String group) {
		commands.execute(printStream, "mk -g /test/");

		CommandLine cmd = commands.buildCommandLine();

		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get(group).getCommandSpec();

		assertNotNull(spec.optionsMap().get("--group").completionCandidates());
		assertEquals("/test/", spec.optionsMap().get("--group").completionCandidates().iterator().next());
	}
	
	@Test
	void move_command_dest_group_option_has_group_completer() {
		commands.execute(printStream, "mk -g /test/");
		
		CommandLine cmd = commands.buildCommandLine();
		
		CommandLine.Model.CommandSpec spec = cmd.getSubcommands().get("move").getCommandSpec();
		
		assertNotNull(spec.optionsMap().get("--dest-group").completionCandidates());
		assertEquals("/test/", spec.optionsMap().get("--dest-group").completionCandidates().iterator().next());
	}

	@Test
	void invalid_group_path() {
		commands.execute(printStream, "ch -g /project/test");
		
		assertOutput(
				"'/project/test' is not a valid group path",
				""
		);
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void mklist_command_help(String parameter) {
		commands.execute(printStream, "mk " + parameter);

		assertOutput(
				"Usage:  mk (-l=<list> | -g=<group>) [-h]",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>"
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void chlist_command_help(String parameter) {
		commands.execute(printStream, "ch " + parameter);

		assertOutput(
				"Usage:  ch (-l=<list> | -g=<group>) [-h]",
				"  -g, --group=<group>",
				"  -h, --help            Show this help message.",
				"  -l, --list=<list>"
		);
	}
}
