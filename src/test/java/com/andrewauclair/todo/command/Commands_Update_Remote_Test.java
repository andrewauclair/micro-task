// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.todo.UtilsTest.byteInStream;
import static com.andrewauclair.todo.UtilsTest.createFile;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Update_Remote_Test extends CommandsBaseTestCase {
	@Test
	void update_to_remote_runs_git_push() {
		commands.execute(printStream, "update --to-remote");

		Mockito.verify(osInterface).runGitCommand("git push", false);

		assertOutput(
				"Pushed changes to remote",
				""
		);
	}

	@Test
	void update_from_remote_runs_git_pull() {
		commands.execute(printStream, "update --from-remote");

		Mockito.verify(osInterface).runGitCommand("git pull", false);

		assertOutput(
				"Pulled changes from remote",
				""
		);
	}

	@Test
	void pulling_changes_from_remote_reloads_tasks() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("test", "git-data/tasks/test", true)
				)
		);

		Mockito.when(osInterface.listFiles("git-data/tasks/test")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("list.txt", "git-data/tasks/test/list.txt", false),
						new OSInterface.TaskFileInfo("1.txt", "git-data/tasks/test/1.txt", false)
				)
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/test/list.txt")).thenReturn(
				byteInStream(createFile("Project X", "Feature Y", "InProgress"))
		);

		String contents = "Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL + Utils.NL +
				"add 1000" + Utils.NL;

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/tasks/test/1.txt")).thenReturn(inputStream);

		commands.execute(printStream, "update --from-remote");

		assertTrue(tasks.hasTaskWithID(1));
	}
}
