// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static com.andrewauclair.microtask.TestUtils.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskLoader_TaskContainerState_Test extends TaskBaseTestCase {
	private final TaskReader reader = Mockito.mock(TaskReader.class);
	private final Projects projects = Mockito.mock(Projects.class);
	private TaskLoader loader;

	@BeforeEach
	protected void setup() throws IOException {
		super.setup();

		LocalSettings localSettings = Mockito.mock(LocalSettings.class);

		loader = new TaskLoader(tasks, reader, localSettings, projects, osInterface);

		Mockito.when(reader.readTask(Mockito.anyLong(), Mockito.anyString())).thenAnswer(invocation -> newTask(invocation.getArgument(0), "Test", TaskState.Inactive, 0));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not write files"));
		Mockito.doThrow(new RuntimeException("TaskLoader should not run git commands")).when(osInterface).gitCommit(Mockito.anyString());
	}

	@Test
	@Disabled
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
				createInputStream("InProgress")
		);

		loader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/one/").getState());

	}

	// TODO We can remove this once the new version is out
	@Test
	@Disabled
	void load_state_for_groups__legacy() throws IOException {
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
				createInputStream("", "" , "InProgress")
		);

		loader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/one/").getState());
	}
	@Test
	@Disabled
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
				createInputStream("InProgress")
		);

		loader.load();

		assertEquals(TaskContainerState.InProgress, tasks.getListByName(existingList("/two")).getState());
	}
}
