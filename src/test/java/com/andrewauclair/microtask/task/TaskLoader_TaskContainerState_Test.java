// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

import static com.andrewauclair.microtask.UtilsTest.byteInStream;
import static com.andrewauclair.microtask.UtilsTest.createFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskLoader_TaskContainerState_Test extends TaskBaseTestCase {
	private TaskReader reader = Mockito.mock(TaskReader.class);
	private TaskLoader loader;

	@BeforeEach
	void setup() throws IOException {
		super.setup();

		loader = new TaskLoader(tasks, reader, osInterface);

		Mockito.when(reader.readTask(Mockito.anyLong(), Mockito.anyString())).thenAnswer(invocation -> new Task(invocation.getArgument(0), "Test", TaskState.Inactive, Collections.emptyList()));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not write files"));
		Mockito.when(osInterface.runGitCommand(Mockito.anyString(), Mockito.anyBoolean())).thenThrow(new RuntimeException("TaskLoader should not run git commands"));
	}

	@Test
	void load_state_for_groups() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("one", "git-data/tasks/one", true)
				)
		);

		Mockito.when(osInterface.listFiles("git-data/tasks/one")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("group.txt", "git-data/tasks/one/group.txt", false)
				)
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/one/group.txt")).thenReturn(
				byteInStream(createFile("", "", "Active"))
		);

		loader.load();

		assertEquals(TaskContainerState.Active, tasks.getGroup("/one/").getState());

	}

	@Test
	void load_state_for_lists() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("two", "git-data/tasks/two", true)
				)
		);

		Mockito.when(osInterface.listFiles("git-data/tasks/two")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("list.txt", "git-data/tasks/two/list.txt", false)
				)
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/two/list.txt")).thenReturn(
				byteInStream(createFile("", "", "Active"))
		);

		loader.load();

		assertEquals(TaskContainerState.Active, tasks.getListByName("/two").getState());
	}
}
