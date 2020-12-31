// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupTest extends TaskBaseTestCase {
	@Test
	void task_group_can_contain_other_task_groups() {
		TaskGroup group = new TaskGroup("/test");
		
		TaskGroup child = new TaskGroup("two", group, TaskContainerState.InProgress);
		group.addChild(child);
		
		assertThat(group.getChildren()).containsOnly(
				child
		);
	}
	
	@Test
	void task_group_can_contain_task_lists() {
		TaskGroup group = new TaskGroup("test");
		
		TaskList list = new TaskList("test", new TaskGroup("/"), osInterface, writer, TaskContainerState.InProgress);
		group.addChild(list);
		
		assertThat(group.getChildren()).containsOnly(
				list
		);
	}

	@Test
	void create_root_task_group() {
		TaskGroup group = new TaskGroup("/");

		assertEquals("/", group.getName());
		assertEquals("/", group.getFullPath());
	}

	@Test
	void create_task_group() {
		TaskGroup parent = new TaskGroup("/two/three/");
		TaskGroup group = new TaskGroup("one", parent, TaskContainerState.InProgress);

		assertEquals("one", group.getName());
		assertEquals("/two/three/one/", group.getFullPath());
//		assertEquals("Project", group.getProject());
//		assertEquals("Feature", group.getFeature());
	}

	@Test
	void to_string() {
		TaskGroup parent = new TaskGroup("/");
		TaskGroup group = new TaskGroup("test", parent, TaskContainerState.InProgress);
		
		assertEquals("TaskGroup{name='test', fullPath='/test/', parent='/', children=[]}", group.toString());
	}

	@Test
	void to_string_with_null_parent() {
		TaskGroup group = new TaskGroup("/", null, TaskContainerState.InProgress);

		assertEquals("TaskGroup{name='/', fullPath='/', parent='', children=[]}", group.toString());
	}
	
	@Test
	void rename() {
		TaskGroup parent = new TaskGroup("/");
		TaskGroup group = new TaskGroup("test", parent, TaskContainerState.InProgress);
		
		TaskGroup renamed = group.rename("one");
		
		assertEquals("TaskGroup{name='one', fullPath='/one/', parent='/', children=[]}", renamed.toString());
	}
}
