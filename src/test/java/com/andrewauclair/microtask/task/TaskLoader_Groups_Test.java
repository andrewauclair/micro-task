// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static com.andrewauclair.microtask.TestUtils.newTask;

class TaskLoader_Groups_Test extends TaskBaseTestCase {
	private final TaskReader reader = Mockito.mock(TaskReader.class);
	private final LocalSettings localSettings = Mockito.mock(LocalSettings.class);
	private final Projects projects = Mockito.mock(Projects.class);
	private final TaskLoader loader = new TaskLoader(tasks, reader, localSettings, projects, osInterface);

	@BeforeEach
	protected void setup() throws IOException {
		Mockito.when(reader.readTask(Mockito.anyLong(), Mockito.anyString())).thenAnswer(invocation -> newTask(invocation.getArgument(0), "Test", TaskState.Inactive, 0));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not write files"));
		Mockito.doThrow(new RuntimeException("TaskLoader should not run git commands")).when(osInterface).gitCommit(Mockito.anyString());
	}

	@Test
	@Disabled("This actually seems redundant now that we throw exceptions for these function calls")
	void task_loader_does_not_create_groups() throws IOException {
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
				createInputStream("")
		);

		Mockito.reset(osInterface);
		
		loader.load();

		Mockito.verify(osInterface, Mockito.never()).gitCommit(Mockito.anyString());
		Mockito.verify(osInterface, Mockito.never()).createOutputStream(Mockito.anyString());
	}
}
