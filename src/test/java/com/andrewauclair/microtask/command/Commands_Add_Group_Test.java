// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.task.TaskGroupFinder;
import com.andrewauclair.microtask.task.TaskGroupName;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Add_Group_Test extends CommandsBaseTestCase {
	@Test
	void create_group_command() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(listStream));

		commands.execute(printStream, "add group /test/one/two/three/ --time-category overhead-general");

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new ExistingGroupName(tasks, "/test/one/two/three/")));

		InOrder inOrder = Mockito.inOrder(osInterface);

		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/one/group.txt");
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/one/two/group.txt");
		inOrder.verify(osInterface).createOutputStream("git-data/tasks/test/one/two/three/group.txt");
		inOrder.verify(osInterface).gitCommit("Created group '/test/one/two/three/'");

		TestUtils.assertOutput(listStream,
				"state InProgress",
				"time overhead-general",
				""
		);

		assertOutput(
				"Created group '/test/one/two/three/' with Time Category 'overhead-general'",
				""
		);
	}

	@Test
	void create_relative_group() {
		tasks.createGroup(newGroup("one/"));
		tasks.setCurrentGroup(existingGroup("one/"));

		commands.execute(printStream, "add group two/three/");

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new ExistingGroupName(tasks, "/one/two/three/")));

		assertOutput(
				"Created group '/one/two/three/'",
				""
		);
	}
}
