// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.todo.Utils.NL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskLoader_ProjectFeature_Test extends TaskBaseTestCase {
	private TaskReader reader = Mockito.mock(TaskReader.class);
	private TaskLoader loader = new TaskLoader(tasks, reader, osInterface);
	
	@Test
	void load_projects_and_features_for_groups() throws IOException {
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
				new ByteArrayInputStream(("Project X" + NL + "Feature Y").getBytes())
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
				new ByteArrayInputStream(("" + NL + "" + NL).getBytes())
		);
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/one/two/group.txt")).thenReturn(
				new ByteArrayInputStream(("Project X" + NL + "Feature Y").getBytes())
		);
		
		loader.load();
		
		assertEquals("Project X", tasks.getGroup("/one/two/").getProject());
		assertEquals("Feature Y", tasks.getGroup("/one/two/").getFeature());
	}
	
	@Test
	void load_projects_and_features_for_lists() throws IOException {
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
				new ByteArrayInputStream(("Project X" + NL + "Feature Y").getBytes())
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
