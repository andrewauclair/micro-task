// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

class TaskLoader_Groups_Test extends TaskBaseTestCase {
	private TaskReader reader = Mockito.mock(TaskReader.class);
	private TaskLoader loader = new TaskLoader(tasks, reader, osInterface);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(reader.readTask(Mockito.anyLong(), Mockito.anyString())).thenAnswer(invocation -> new Task(invocation.getArgument(0), "Test", TaskState.Inactive, Collections.emptyList()));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not write files"));
		Mockito.when(osInterface.runGitCommand(Mockito.anyString(), Mockito.anyBoolean())).thenThrow(new RuntimeException("TaskLoader should not run git commands"));
	}

	@Test
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
				new ByteArrayInputStream("".getBytes())
		);

		Mockito.reset(osInterface);
		
		loader.load();

		Mockito.verify(osInterface, Mockito.never()).runGitCommand(Mockito.anyString(), Mockito.anyBoolean());
		Mockito.verify(osInterface, Mockito.never()).createOutputStream(Mockito.anyString());
	}
}
