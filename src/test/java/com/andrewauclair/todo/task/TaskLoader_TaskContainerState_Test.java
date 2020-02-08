// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.todo.Utils.NL;
import static com.andrewauclair.todo.UtilsTest.byteInStream;
import static com.andrewauclair.todo.UtilsTest.createFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskLoader_TaskContainerState_Test extends TaskBaseTestCase {
	private TaskReader reader = Mockito.mock(TaskReader.class);
	private TaskLoader loader = new TaskLoader(tasks, reader, osInterface);

	@BeforeEach
	void setup() throws IOException {
//		super.setup();
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
				Arrays.asList(
						new OSInterface.TaskFileInfo("1.txt", "git-data/tasks/one/1.txt", false),
						new OSInterface.TaskFileInfo("group.txt", "git-data/tasks/one/group.txt", false)
				)
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/one/group.txt")).thenReturn(
				byteInStream(createFile("", "", "Active"))
		);

		InputStream inputStream = byteInStream(createFile(
				"Test",
				"Inactive",
				"false",
				"",
				"",
				"add 123",
				"start 1234",
				"Project 1",
				"Feature 1",
				"stop 4567",
				"start 3333",
				"Project 2",
				"Feature 2",
				"stop 5555"
		));

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		loader.load();

		assertEquals(TaskContainerState.Active, tasks.getGroup("/one/").getState());

		assertNotNull(tasks.getTask(1));
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
