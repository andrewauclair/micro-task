// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupTest extends TaskBaseTestCase {
	@Test
	void task_group_can_contain_other_task_groups() {
		TaskGroup group = new TaskGroup("/test");
		
		TaskGroup child = new TaskGroup("two", group, "", "", TaskContainerState.InProgress);
		group.addChild(child);
		
		assertThat(group.getChildren()).containsOnly(
				child
		);
	}
	
	@Test
	void task_group_can_contain_task_lists() {
		TaskGroup group = new TaskGroup("test");
		
		TaskList list = new TaskList("test", new TaskGroup("/"), osInterface, writer, "", "", TaskContainerState.InProgress);
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
		TaskGroup group = new TaskGroup("one", parent, "Project", "Feature", TaskContainerState.InProgress);

		assertEquals("one", group.getName());
		assertEquals("/two/three/one/", group.getFullPath());
		assertEquals("Project", group.getProject());
		assertEquals("Feature", group.getFeature());
	}

	@Test
	void to_string() {
		TaskGroup parent = new TaskGroup("/");
		TaskGroup group = new TaskGroup("test", parent, "Project", "Feature", TaskContainerState.InProgress);
		
		assertEquals("TaskGroup{name='test', fullPath='/test/', parent=/, children=[], project='Project', feature='Feature'}", group.toString());
	}
	
	@Test
	void rename() {
		TaskGroup parent = new TaskGroup("/");
		TaskGroup group = new TaskGroup("test", parent, "Project", "Feature", TaskContainerState.InProgress);
		
		TaskGroup renamed = group.rename("one");
		
		assertEquals("TaskGroup{name='one', fullPath='/one/', parent=/, children=[], project='Project', feature='Feature'}", renamed.toString());
	}
}
