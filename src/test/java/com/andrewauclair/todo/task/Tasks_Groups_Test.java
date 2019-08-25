// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Groups_Test extends CommandsBaseTestCase {
	@Test
	void starting_group_path_is_root() {
		assertEquals("/", tasks.getGroupPath());
	}
	
	@Test
	void has_root_path() {
		assertTrue(tasks.hasGroupPath("/"));
	}
	
	@Test
	void does_not_have_group_that_does_not_exist() {
		assertFalse(tasks.hasGroupPath("/test"));
	}
	
	@Test
	void group_paths_need_to_start_with_root() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.hasGroupPath("test"));
		
		assertEquals("Group path must start with root (/).", runtimeException.getMessage());
	}
	
	@Test
	void create_new_group() {
		tasks.createGroup("/test");
		
		assertTrue(tasks.hasGroupPath("/test"));
	}
	
	@Test
	void switch_groups() {
		tasks.createGroup("/test");
		
		tasks.switchGroup("/test");
		
		assertEquals("/test", tasks.getGroupPath());
	}
	
	@Test
	void active_group_is_root_by_default() {
		TaskGroup group = tasks.getActiveGroup();
		
		assertEquals("/", group.getName());
	}
	
	@Test
	void new_active_group_has_root_as_parent() {
		tasks.createGroup("/test");
		
		tasks.switchGroup("/test");
		
		TaskGroup group = tasks.getActiveGroup();

		assertEquals("test", group.getName());
		assertEquals("/", group.getParent());

		assertThat(tasks.getRootGroup().getChildren()).containsOnly(
				new TaskList("/default", osInterface, writer),
				new TaskGroup("test", "/")
		);
	}

	@Test
	void group_is_only_added_once_when_creating_nested_group() {
		tasks.createGroup("/test");
		tasks.createGroup("/test/one");
		tasks.createGroup("/test/one/two");

		TaskGroup expected = new TaskGroup("test", "/");
		TaskGroup one = new TaskGroup("one", "/test");
		expected.addChild(one);
		one.addChild(new TaskGroup("two", "/test/one"));

		assertThat(tasks.getRootGroup().getChildren()).containsOnly(
				new TaskList("/default", osInterface, writer),
				expected
		);
	}

	@Test
	void nested_groups_have_a_parent_that_is_not_root() {
		tasks.createGroup("/test/two");
		
		tasks.switchGroup("/test/two");
		
		TaskGroup group = tasks.getActiveGroup();

		assertEquals("/test", group.getParent());
	}
	
	@Test
	void switch_group_fails_if_group_path_does_not_exist() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.switchGroup("/test"));
		
		assertEquals("Group '/test' does not exist.", runtimeException.getMessage());
		
		// path should not have changed
		assertEquals("/", tasks.getGroupPath());
	}
	
	@Test
	void catch_create_group_io_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);
		
		tasks.createGroup("/one/two");
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL + "java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
}
