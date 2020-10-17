// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskFinder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Update_Remote_Test extends CommandsBaseTestCase {
	@Test
	void update_to_remote_runs_git_push() {
		commands.execute(printStream, "update repo --to-remote");

		Mockito.verify(osInterface).gitPush();

		assertOutput(
				"Pushed changes to remote",
				""
		);
	}

	@Test
	void update_from_remote_runs_git_pull() throws IOException {
		tasks.addGroup(newGroup("/projects/"));

		commands.execute(printStream, "update repo --from-remote");

		Mockito.verify(osInterface).gitPull();

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
				createInputStream("InProgress")
		);

		InputStream inputStream = createInputStream(
				"Test",
				"Inactive",
				"false",
				"",
				"add 1000",
				"END",
				""
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/test/1.txt")).thenReturn(inputStream);

		commands.execute(printStream, "update repo --from-remote");

		TaskFinder finder = new TaskFinder(tasks);
		assertTrue(finder.hasTaskWithID(1));
	}

	@Test
	void updating_to_remote_does_not_get_releases() {
		commands.execute(printStream, "update repo --to-remote");

		Mockito.verifyNoInteractions(gitLabReleases);
	}

	@Test
	void updating_from_remote_does_not_get_releases() {
		commands.execute(printStream, "update repo --from-remote");

		Mockito.verifyNoInteractions(gitLabReleases);
	}
}
