// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.UtilsTest.byteInStream;
import static com.andrewauclair.microtask.UtilsTest.createFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskLoader_ProjectFeature_Test extends TaskBaseTestCase {
	private TaskReader reader = Mockito.mock(TaskReader.class);
	private TaskLoader loader;
	
	@BeforeEach
	void setup() throws IOException {
		super.setup();

		LocalSettings localSettings = Mockito.mock(LocalSettings.class);

		loader = new TaskLoader(tasks, reader, localSettings, osInterface);

		Mockito.when(reader.readTask(Mockito.anyLong(), Mockito.anyString())).thenAnswer(invocation -> new Task(invocation.getArgument(0), "Test", TaskState.Inactive, Collections.emptyList()));
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not write files"));
		Mockito.when(osInterface.runGitCommand(Mockito.anyString())).thenThrow(new RuntimeException("TaskLoader should not run git commands"));
	}

	// TODO We don't need to support this format anymore
	@Test
	void load_projects_and_features_for_groups_old_format() throws IOException {
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
				byteInStream(createFile("Project X", "Feature Y"))
		);

		loader.load();

		assertEquals("Project X", tasks.getGroup("/one/").getProject());
		assertEquals("Feature Y", tasks.getGroup("/one/").getFeature());
	}

	@Test
	void load_projects_and_features_for_groups_new_format() throws IOException {
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
				byteInStream(createFile("Project X", "Feature Y", "InProgress"))
		);
		
		loader.load();
		
		assertEquals("Project X", tasks.getGroup("/one/").getProject());
		assertEquals("Feature Y", tasks.getGroup("/one/").getFeature());
	}

	@Test
	void load_projects_and_features_for_nested_group() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("one", "git-data/tasks/one", true)
				)
		);
		
		Mockito.when(osInterface.listFiles("git-data/tasks/one")).thenReturn(
				Arrays.asList(
						new OSInterface.TaskFileInfo("group.txt", "git-data/tasks/one/group.txt", false),
						new OSInterface.TaskFileInfo("two", "git-data/tasks/one/two", true)
				)
		);
		
		Mockito.when(osInterface.listFiles("git-data/tasks/one/two")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("group.txt", "git-data/tasks/one/two/group.txt", false)
				)
		);
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/group.txt")).thenReturn(
				byteInStream(createFile("", "", "InProgress"))
		);
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/two/group.txt")).thenReturn(
				byteInStream(createFile("Project X", "Feature Y", "Finished"))
		);
		
		loader.load();
		
		assertEquals("Project X", tasks.getGroup("/one/two/").getProject());
		assertEquals("Feature Y", tasks.getGroup("/one/two/").getFeature());
	}

	@Test
	void load_projects_and_features_for_lists_old_format() throws IOException {
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
				byteInStream(createFile("Project X", "Feature Y"))
		);

		loader.load();

		assertEquals("Project X", tasks.getListByName("/two").getProject());
		assertEquals("Feature Y", tasks.getListByName("/two").getFeature());
	}

	@Test
	void load_projects_and_features_for_lists_new_format() throws IOException {
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
				byteInStream(createFile("Project X", "Feature Y", "Finished"))
		);
		
		loader.load();
		
		assertEquals("Project X", tasks.getListByName("/two").getProject());
		assertEquals("Feature Y", tasks.getListByName("/two").getFeature());
	}
	
	@Test
	void handles_loading_original_group_txt_files_that_were_empty() throws IOException {
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
		
		loader.load();
		
		assertEquals("", tasks.getGroup("/one/").getProject());
		assertEquals("", tasks.getGroup("/one/").getFeature());
	}
	
	@Test
	void failing_to_read_group_txt_does_not_stop_reading_all_data() throws IOException {
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
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/group.txt")).thenThrow(IOException.class);
		
		loader.load();
		
		assertEquals("", tasks.getGroup("/one/").getProject());
		assertEquals("", tasks.getGroup("/one/").getFeature());
	}
	
	@Test
	void failing_to_read_list_txt_does_not_stop_reading_all_data() throws IOException {
		Mockito.when(osInterface.listFiles("git-data/tasks")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("one", "git-data/tasks/one", true)
				)
		);
		
		Mockito.when(osInterface.listFiles("git-data/tasks/one")).thenReturn(
				Collections.singletonList(
						new OSInterface.TaskFileInfo("list.txt", "git-data/tasks/one/list.txt", false)
				)
		);
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/list.txt")).thenThrow(IOException.class);
		
		loader.load();
		
		assertEquals("", tasks.getListByName("/one").getProject());
		assertEquals("", tasks.getListByName("/one").getFeature());
	}
}
