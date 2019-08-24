// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupTest extends TaskBaseTestCase {
	@Test
	void task_group_can_contain_other_task_groups() {
		TaskGroup group = new TaskGroup("/test");

		TaskGroup child = new TaskGroup("two", "/test");
		group.addChild(child);
		
		assertThat(group.getChildren()).containsOnly(
				child
		);
	}
	
	@Test
	void task_group_can_contain_task_lists() {
		TaskGroup group = new TaskGroup("test");
		
		TaskList list = new TaskList("test", osInterface);
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
		TaskGroup group = new TaskGroup("one", "/two/three");

		assertEquals("one", group.getName());
		assertEquals("/two/three/one", group.getFullPath());
	}

	@Test
	void to_string() {
		TaskGroup group = new TaskGroup("test", "/");

		assertEquals("TaskGroup{name='test', fullPath='/test', parent=/, children=[]}", group.toString());
	}
	
	@Test
	void rename() {
		TaskGroup group = new TaskGroup("test", "/");
		
		TaskGroup renamed = group.rename("one");
		
		assertEquals("TaskGroup{name='one', fullPath='/one', parent=/, children=[]}", renamed.toString());
	}
}
