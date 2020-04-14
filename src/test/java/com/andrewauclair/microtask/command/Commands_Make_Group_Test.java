// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Make_Group_Test extends CommandsBaseTestCase {
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
		inOrder.verify(osInterface).runGitCommand("git add .");
		inOrder.verify(osInterface).runGitCommand("git commit -m \"Created group '/test/one/two/three/'\"");

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
	void create_relative_group() {
		tasks.createGroup("one/");
		tasks.setActiveGroup("one/");


		commands.execute(printStream, "mk -g two/three/");

		assertTrue(tasks.hasGroupPath("/one/two/three/"));

		assertOutput(
				"Created group '/one/two/three/'",
				""
		);
	}
}
