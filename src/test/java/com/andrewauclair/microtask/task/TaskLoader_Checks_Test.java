// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskLoader_Checks_Test extends TaskBaseTestCase {
	private final TaskReader reader = Mockito.mock(TaskReader.class);
	private TaskLoader loader;

	@BeforeEach
	protected void setup() throws IOException {
		super.setup();

		LocalSettings localSettings = Mockito.mock(LocalSettings.class);

		loader = new TaskLoader(tasks, reader, localSettings, osInterface);
	}

	@Test
	void task_loader_throws_exception_for_unknown_file_in_list_folder() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("test", "git-data/tasks/test", true)
				)
		);

		Mockito.when(osInterface.listFiles("git-data/tasks/test")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("list.txt", "git-data/tasks/test/list.txt", false),
						new OSInterface.TaskFileInfo("junk.txt", "git-data/tasks/test/junk.txt", false),
						new OSInterface.TaskFileInfo("1.txt", "git-data/tasks/test/1.txt", false)
				)
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/test/list.txt")).thenReturn(
				createInputStream("Project X", "Feature Y", "InProgress")
		);

		TaskException taskException = assertThrows(TaskException.class, () -> loader.load());

		assertEquals("Unexpected file 'git-data/tasks/test/junk.txt'", taskException.getMessage());
	}

	@Test
	void task_loader_throws_exception_for_unknown_file_in_group_folder() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("test", "git-data/tasks/test", true)
				)
		);

		Mockito.when(osInterface.listFiles("git-data/tasks/test")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("group.txt", "git-data/tasks/test/group.txt", false),
						new OSInterface.TaskFileInfo("1.txt", "git-data/tasks/test/1.txt", false)
				)
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/test/group.txt")).thenReturn(
				createInputStream("Project X", "Feature Y", "Finished")
		);

		TaskException taskException = assertThrows(TaskException.class, () -> loader.load());

		assertEquals("Unexpected file 'git-data/tasks/test/1.txt'", taskException.getMessage());
	}
}
