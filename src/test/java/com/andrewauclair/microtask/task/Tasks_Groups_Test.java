// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;

class Tasks_Groups_Test extends CommandsBaseTestCase {
	@Test
	void const_root_path() {
		assertEquals(TaskGroup.ROOT_PATH, TaskGroup.ROOT_PATH);
	}
	
	@Test
	void starting_group_path_is_root() {
		assertEquals(TaskGroup.ROOT_PATH, tasks.getCurrentGroup().getFullPath());
	}
	
	@Test
	void has_root_path() {
		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, TaskGroup.ROOT_PATH)));
	}
	
	@Test
	void does_not_have_group_that_does_not_exist() {
		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertFalse(finder.hasGroupPath(new TaskGroupName(tasks, "/test/")));
	}
	
	@Test
	void create_new_group() {
		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		tasks.createGroup(newGroup("/test/"));
		
		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/test/")));
	}
	
	@Test
	void switch_groups() {
		tasks.createGroup(newGroup("/test/"));
		
		tasks.setCurrentGroup(existingGroup("/test/"));
		
		assertEquals("/test/", tasks.getCurrentGroup().getFullPath());
	}
	
	@Test
	void active_group_is_root_by_default() {
		TaskGroup group = tasks.getCurrentGroup();
		
		assertEquals(TaskGroup.ROOT_PATH, group.getName());
	}
	
	@Test
	void new_active_group_has_root_as_parent() {
		tasks.createGroup(newGroup("/test/"));
		
		tasks.setCurrentGroup(existingGroup("/test/"));
		
		TaskGroup group = tasks.getCurrentGroup();

		assertEquals("test", group.getName());
		assertEquals(TaskGroup.ROOT_PATH, group.getParent());

		TaskGroup parent = new TaskGroup(TaskGroup.ROOT_PATH);

		assertThat(tasks.getRootGroup().getChildren()).containsOnly(
				new TaskList("default", new TaskGroup(TaskGroup.ROOT_PATH), osInterface, writer, TaskContainerState.InProgress, "none"),
				new TaskGroup("test", parent, TaskContainerState.InProgress)
		);
	}

	@Test
	void group_is_only_added_once_when_creating_nested_group() {
		tasks.createGroup(newGroup("/test/"));
		tasks.createGroup(newGroup("/test/one/"));
		tasks.createGroup(newGroup("/test/one/two/"));

		TaskGroup parent = new TaskGroup(TaskGroup.ROOT_PATH);
		parent.addChild(new TaskList("default", new TaskGroup(TaskGroup.ROOT_PATH), osInterface, writer, TaskContainerState.InProgress, "none"));
		
		TaskGroup expected = new TaskGroup("test", parent, TaskContainerState.InProgress);
		parent.addChild(expected);
		
		TaskGroup one = new TaskGroup("one", expected, TaskContainerState.InProgress);
		expected.addChild(one);
		
		one.addChild(new TaskGroup("two", one, TaskContainerState.InProgress));

		assertThat(tasks.getRootGroup().getChildren()).containsOnly(
				new TaskList("default", new TaskGroup(TaskGroup.ROOT_PATH), osInterface, writer, TaskContainerState.InProgress, "none"),
				expected
		);
	}

	@Test
	void nested_groups_have_a_parent_that_is_not_root() {
		tasks.createGroup(newGroup("/test/two/"));
		
		tasks.setCurrentGroup(existingGroup("/test/two/"));
		
		TaskGroup group = tasks.getCurrentGroup();
		
		assertEquals("/test/", group.getParent());
	}
	
	@Test
	void switch_group_fails_if_group_path_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setCurrentGroup(existingGroup("/test/")));
		
		assertEquals("Group '/test/' does not exist.", taskException.getMessage());
		
		// path should not have changed
		assertEquals(TaskGroup.ROOT_PATH, tasks.getCurrentGroup().getFullPath());
	}
	
	@Test
	void catch_create_group_io_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);
		
		tasks.createGroup(newGroup("/one/two/"));
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL + "java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
	
	@Test
	void group_for_list_root() {
		TaskGroup groupForList = tasks.getGroupForList(existingList("/default"));
		
		assertEquals(tasks.getRootGroup(), groupForList);
	}

	@Test
	void throws_exception_when_creating_group_if_parent_group_has_been_finished() {
		tasks.addGroup(newGroup("/test/"));
		tasks.finishGroup(existingGroup("/test/"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.createGroup(newGroup("/test/one/")));

		assertEquals("Group '/test/one/' cannot be created because group '/test/' has been finished.", taskException.getMessage());

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertFalse(finder.hasGroupPath(new TaskGroupName(tasks, "/test/one/")));

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void does_not_throw_exception_for_finished_group_when_not_creating_files() {
		tasks.addGroup(newGroup("/test/"));
		tasks.finishGroup(existingGroup("/test/"));

		Mockito.reset(writer, osInterface);

		assertDoesNotThrow(() -> tasks.addGroup(newGroup("/test/one/")));

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/test/one/")));

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void throws_exception_if_list_to_get_group_for_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.getGroupForList(existingList("/test")));

		assertEquals("List '/test' does not exist.", taskException.getMessage());
	}
	
	@Test
	void add_group_without_creating_folder_or_adding_to_git__used_for_reloading_groups() {
		Mockito.reset(osInterface);

		tasks.addGroup(newGroup("/one/two/"));

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		assertTrue(finder.hasGroupPath(new TaskGroupName(tasks, "/one/two/")));
		
		Mockito.verifyNoInteractions(osInterface);
	}
	
	@Test
	void does_not_commit_files_for_group_that_already_exists() throws IOException {
		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/two"), true);
		
		Mockito.reset(osInterface);

		OutputStream listStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/one/three/list.txt")).thenReturn(new DataOutputStream(listStream));

		tasks.addList(newList("/one/three"), true);
		
		Mockito.verify(osInterface, never()).gitCommit("Created group '/one/'");
	}
}
