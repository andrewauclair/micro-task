// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
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
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.hasGroupPath("test"));
		
		assertEquals("Group path must start with root (/).", taskException.getMessage());
	}
	
	@Test
	void create_new_group() {
		tasks.createGroup("/test/");
		
		assertTrue(tasks.hasGroupPath("/test/"));
	}
	
	@Test
	void switch_groups() {
		tasks.createGroup("/test/");
		
		tasks.switchGroup("/test/");
		
		assertEquals("/test/", tasks.getGroupPath());
	}
	
	@Test
	void active_group_is_root_by_default() {
		TaskGroup group = tasks.getActiveGroup();
		
		assertEquals("/", group.getName());
	}
	
	@Test
	void new_active_group_has_root_as_parent() {
		tasks.createGroup("/test/");
		
		tasks.switchGroup("/test/");
		
		TaskGroup group = tasks.getActiveGroup();

		assertEquals("test", group.getName());
		assertEquals("/", group.getParent());

		TaskGroup parent = new TaskGroup("/");

		assertThat(tasks.getRootGroup().getChildren()).containsOnly(
				new TaskList("default", new TaskGroup("/"), osInterface, writer, "", "", TaskContainerState.InProgress),
				new TaskGroup("test", parent, "", "", TaskContainerState.InProgress)
		);
	}

	@Test
	void group_is_only_added_once_when_creating_nested_group() {
		tasks.createGroup("/test/");
		tasks.createGroup("/test/one/");
		tasks.createGroup("/test/one/two/");

		TaskGroup parent = new TaskGroup("/");
		parent.addChild(new TaskList("default", new TaskGroup("/"), osInterface, writer, "", "", TaskContainerState.InProgress));
		
		TaskGroup expected = new TaskGroup("test", parent, "", "", TaskContainerState.InProgress);
		parent.addChild(expected);
		
		TaskGroup one = new TaskGroup("one", expected, "", "", TaskContainerState.InProgress);
		expected.addChild(one);
		
		one.addChild(new TaskGroup("two", one, "", "", TaskContainerState.InProgress));

		assertThat(tasks.getRootGroup().getChildren()).containsOnly(
				new TaskList("default", new TaskGroup("/"), osInterface, writer, "", "", TaskContainerState.InProgress),
				expected
		);
	}

	@Test
	void nested_groups_have_a_parent_that_is_not_root() {
		tasks.createGroup("/test/two/");
		
		tasks.switchGroup("/test/two/");
		
		TaskGroup group = tasks.getActiveGroup();
		
		assertEquals("/test/", group.getParent());
	}
	
	@Test
	void switch_group_fails_if_group_path_does_not_exist() {
		TaskException taskException = assertThrows(TaskException.class, () -> tasks.switchGroup("/test/"));
		
		assertEquals("Group '/test/' does not exist.", taskException.getMessage());
		
		// path should not have changed
		assertEquals("/", tasks.getGroupPath());
	}
	
	@Test
	void catch_create_group_io_exception() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);
		
		tasks.createGroup("/one/two");
		
		Assertions.assertEquals("java.io.IOException" + Utils.NL + "java.io.IOException" + Utils.NL, this.outputStream.toString());
	}
	
	@Test
	void group_for_list_root() {
		TaskGroup groupForList = tasks.getGroupForList("/default");
		
		assertEquals(tasks.getRootGroup(), groupForList);
	}

	@Test
	void group_gets_project_from_parent_if_project_is_an_empty_string() {
		TaskGroup parent = new TaskGroup("root", null, "Project", "", TaskContainerState.InProgress);
		TaskGroup child = new TaskGroup("child", parent, "", "", TaskContainerState.InProgress);

		assertEquals("Project", child.getProject());
	}

	@Test
	void group_gets_feature_from_parent_if_feature_is_an_empty_string() {
		TaskGroup parent = new TaskGroup("root", null, "", "Feature", TaskContainerState.InProgress);
		TaskGroup child = new TaskGroup("child", parent, "", "", TaskContainerState.InProgress);

		assertEquals("Feature", child.getFeature());
	}
	
	@Test
	void add_group_without_creating_folder_or_adding_to_git__used_for_reloading_groups() {
		tasks.addGroup("/one/two/");
		
		assertTrue(tasks.hasGroupPath("/one/two/"));
		
		Mockito.verifyZeroInteractions(osInterface);
	}
}
