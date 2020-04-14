// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.createInputStream;

class TaskLoaderTest extends TaskBaseTestCase {
//	Tasks tasks = Mockito.mock(Tasks.class);
	private final TaskReader reader = Mockito.mock(TaskReader.class);
	private final LocalSettings localSettings = Mockito.mock(LocalSettings.class);
	private TaskLoader loader;// = new TaskLoader(tasks, reader, localSettings, osInterface);
	
	@BeforeEach
	protected void setup() throws IOException {
		super.setup();
		Mockito.when(reader.readTask(Mockito.anyLong(), Mockito.anyString())).thenAnswer(invocation -> new Task(invocation.getArgument(0), "Test", TaskState.Inactive, Collections.emptyList()));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not write files"));
		Mockito.when(osInterface.runGitCommand(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not run git commands"));

//		Mockito.when(tasks.getActiveGroup()).thenReturn(new TaskGroup("/"));
		loader = new TaskLoader(tasks, reader, localSettings, osInterface);
	}
	
	@Test
	@Disabled("Add this back, I really tasks as a mock for this and I can't have it as a mock for the rest of these tests")
	void load_files_from_test_list() throws IOException {
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
				createInputStream("Project X", "Feature Y", "InProgress")
		);

		tasks.addList("/test", false);
		
		ExistingTaskListName expectedList = new ExistingTaskListName(tasks, "/test");

		loader.load();

		InOrder order = Mockito.inOrder(tasks, localSettings);

		Mockito.verify(reader).readTask(1, "git-data/tasks/test/1.txt");

		order.verify(tasks).addList("test", false);
		order.verify(tasks).setActiveList(expectedList);
		order.verify(tasks).addTask(new Task(1, "Test", TaskState.Inactive, Collections.emptyList()));
		order.verify(localSettings).load(tasks);
	}
	
	@Test
	@Disabled("Add this back, I really tasks as a mock for this and I can't have it as a mock for the rest of these tests")
	void load_files_from_nested_list_in_group() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("one", "git-data/tasks/one", true),
						new OSInterface.TaskFileInfo("test", "git-data/tasks/test", true)
				)
		);
		
		Mockito.when(osInterface.listFiles("git-data/tasks/test")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("list.txt", "git-data/tasks/test/list.txt", false),
						new OSInterface.TaskFileInfo("1.txt", "git-data/tasks/test/1.txt", false),
						new OSInterface.TaskFileInfo("2.txt", "git-data/tasks/test/2.txt", false)
				)
		);
		
		Mockito.when(osInterface.listFiles("git-data/tasks/one")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("group.txt", "git-data/tasks/one/group.txt", false),
						new OSInterface.TaskFileInfo("two", "git-data/tasks/one/two", true)
				)
		);
		
		Mockito.when(osInterface.listFiles("git-data/tasks/one/two")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("list.txt", "git-data/tasks/one/two/list.txt", false),
						new OSInterface.TaskFileInfo("3.txt", "git-data/tasks/one/two/3.txt", false),
						new OSInterface.TaskFileInfo("4.txt", "git-data/tasks/one/two/4.txt", false)
				)
		);
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/test/list.txt")).thenReturn(
				createInputStream("Project X", "Feature Y", "InProgress")
		);
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/two/list.txt")).thenReturn(
				createInputStream("Project X", "Feature Y", "InProgress")
		);
		
		TaskGroup parent = new TaskGroup("/");
//		Mockito.when(tasks.getActiveGroup()).thenReturn(new TaskGroup("one", parent, "", "", TaskContainerState.InProgress));
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/group.txt")).thenReturn(
				createInputStream("Project X", "Feature Y", "Finished")
		);

//		Mockito.when(tasks.hasListWithName("/one/two")).thenReturn(true);
//		Mockito.when(tasks.hasListWithName("/one/test")).thenReturn(true);
//		Mockito.when(tasks.hasListWithName("/two")).thenReturn(true);

//		Mockito.when(tasks.addGroup("one/")).thenReturn(new TaskGroup("one/", new TaskGroup("/"), "", "", TaskContainerState.InProgress));

		tasks.addList("/two", false);
		tasks.addList("/test", false);
		
		ExistingTaskListName expectedList = new ExistingTaskListName(tasks, "two");
		ExistingTaskListName expectedList2 = new ExistingTaskListName(tasks, "test");

		loader.load();

		InOrder order = Mockito.inOrder(tasks, localSettings);

		Mockito.verify(reader).readTask(1, "git-data/tasks/test/1.txt");
		Mockito.verify(reader).readTask(2, "git-data/tasks/test/2.txt");
		Mockito.verify(reader).readTask(3, "git-data/tasks/one/two/3.txt");
		Mockito.verify(reader).readTask(4, "git-data/tasks/one/two/4.txt");

		order.verify(tasks).addGroup("one/");
		order.verify(tasks).setActiveGroup("one/");
		order.verify(tasks).addList("two", false);
		order.verify(tasks).setActiveList(expectedList);
		order.verify(tasks).addTask(new Task(3, "Test", TaskState.Inactive, Collections.emptyList()));
		order.verify(tasks).addTask(new Task(4, "Test", TaskState.Inactive, Collections.emptyList()));
		order.verify(tasks).setActiveGroup("/");
		order.verify(tasks).addList("test", false);
		order.verify(tasks).setActiveList(expectedList2);
		order.verify(tasks).addTask(new Task(1, "Test", TaskState.Inactive, Collections.emptyList()));
		order.verify(tasks).addTask(new Task(2, "Test", TaskState.Inactive, Collections.emptyList()));
		order.verify(localSettings).load(tasks);
	}
}
