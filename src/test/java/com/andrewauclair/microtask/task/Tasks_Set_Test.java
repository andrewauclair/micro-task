// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class Tasks_Set_Test extends TaskBaseTestCase {
	@Test
	void set_recurring_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setRecurring(existingID(1), true);

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");

		assertTrue(task.isRecurring());
	}

	@Test
	void set_list_project() {
		tasks.addList(newList("/test"), true);
		tasks.setActiveList(existingList("/test"));
		tasks.addTask("Test 1");
		
		tasks.setProject(existingList("/test"), "Project", true);

		assertEquals("Project", tasks.findListForTask(existingID(1)).getProject());
	}
	
	@Test
	void set_group_project() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setActiveList(existingList("/test/one"));
		
		tasks.setProject(existingGroup("/test/"), "Project", true);
		
		assertEquals("Project", tasks.getGroupForList(existingList("/test/one")).getProject());
	}
	
	@Test
	void set_list_feature() {
		tasks.addList(newList("/test"), true);
		tasks.setActiveList(existingList("/test"));
		tasks.addTask("Test 1");
		
		tasks.setFeature(existingList("/test"), "Feature", true);

		assertEquals("Feature", tasks.findListForTask(existingID(1)).getFeature());
	}
	
	@Test
	void set_group_feature() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setActiveList(existingList("/test/one"));
		
		tasks.setFeature(existingGroup("/test/"), "Feature", true);
		
		assertEquals("Feature", tasks.getGroupForList(existingList("/test/one")).getFeature());
	}
	
	@Test
	void set_recurring_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setRecurring(existingID(1), false);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Set recurring for task 1 to false\"");

		assertFalse(task.isRecurring());
	}

	@Test
	void set_list_project_adds_and_commits_list_file_to_git() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));

		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList(newList("/test"), false);
		tasks.setActiveList(existingList("/test"));
		tasks.addTask("Test 1");

		tasks.setProject(existingList("/test"), "Issue", true);
		
		TestUtils.assertOutput(listStream,
				"Issue",
				"",
				"InProgress",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/list.txt");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Set project for list '/test' to 'Issue'\"");
	}
	
	@Test
	void set_group_project_adds_and_commits_group_file_to_git() throws IOException {
		OutputStream groupStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(groupStream));
		
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), false);
		tasks.setActiveList(existingList("/test/one"));
		
		tasks.setProject(existingGroup("/test/"), "Issue", true);
		
		TestUtils.assertOutput(groupStream,
				"Issue",
				"",
				"InProgress",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Set project for group '/test/' to 'Issue'\"");
	}
	
	@Test
	void set_list_feature_adds_and_commits_file_to_git() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));
		
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList(newList("/test"), false);
		tasks.setActiveList(existingList("/test"));
		tasks.addTask("Test 1");
		
		tasks.setFeature(existingList("/test"), "Feature", true);
		
		TestUtils.assertOutput(listStream,
				"",
				"Feature",
				"InProgress",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/list.txt");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Set feature for list '/test' to 'Feature'\"");
	}
	
	@Test
	void set_group_feature_adds_and_commits_file_to_git() throws IOException {
		OutputStream groupStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(groupStream));
		
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), false);
		tasks.setActiveList(existingList("/test/one"));
		
		tasks.setFeature(existingGroup("/test/"), "Feature", true);
		
		TestUtils.assertOutput(groupStream,
				"",
				"Feature",
				"InProgress",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		order.verify(osInterface).runGitCommand("git add .");
		order.verify(osInterface).runGitCommand("git commit -m \"Set feature for group '/test/' to 'Feature'\"");
	}

	@Test
	void exception_is_thrown_when_trying_to_set_recurring_on_finished_task() {
		Task task = tasks.addTask("Test 1");
		tasks.finishTask(existingID(1));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setRecurring(existingID(1), true));

		assertEquals("Cannot set task 1 recurring state. The task has been finished.", taskException.getMessage());

		assertFalse(task.isRecurring());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_feature_on_finished_list() {
		tasks.addList(newList("/one"), true);
		tasks.setFeature(existingList("/one"), "test", true);
		tasks.finishList(existingList("/one"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setFeature(existingList("/one"), "new-feature", true));

		assertEquals("Cannot set feature on list '/one' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getListByName(existingList("/one")).getFeature());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_project_on_finished_list() {
		tasks.addList(newList("/one"), true);
		tasks.setProject(existingList("/one"), "test", true);
		tasks.finishList(existingList("/one"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setProject(existingList("/one"), "new-project", true));

		assertEquals("Cannot set project on list '/one' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getListByName(existingList("/one")).getProject());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_feature_on_finished_group() {
		tasks.addGroup(newGroup("/one/"));
		tasks.setFeature(existingGroup("/one/"), "test", true);
		tasks.finishGroup(existingGroup("/one/"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setFeature(existingGroup("/one/"), "new-project", true));

		assertEquals("Cannot set feature on group '/one/' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getGroup("/one/").getFeature());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_project_on_finished_group() {
		tasks.addGroup(newGroup("/one/"));
		tasks.setProject(existingGroup("/one/"), "test", true);
		tasks.finishGroup(existingGroup("/one/"));

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setProject(existingGroup("/one/"), "new-feature", true));

		assertEquals("Cannot set project on group '/one/' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getGroup("/one/").getProject());

		Mockito.verifyNoInteractions(writer, osInterface);
	}
}
