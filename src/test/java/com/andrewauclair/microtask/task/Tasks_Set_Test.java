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

		Task task = tasks.setRecurring(1, true);

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");

		assertTrue(task.isRecurring());
	}

	@Test
	void set_list_project() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		tasks.setProject(tasks.findListForTask(1), "Project", true);

		assertEquals("Project", tasks.findListForTask(1).getProject());
	}
	
	@Test
	void set_group_project() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		
		tasks.setProject(tasks.getGroupForList("/test/one"), "Project", true);
		
		assertEquals("Project", tasks.getGroupForList("/test/one").getProject());
	}
	
	@Test
	void set_list_feature() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		tasks.setFeature(tasks.findListForTask(1), "Feature", true);

		assertEquals("Feature", tasks.findListForTask(1).getFeature());
	}
	
	@Test
	void set_group_feature() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		
		tasks.setFeature(tasks.getGroupForList("/test/one"), "Feature", true);
		
		assertEquals("Feature", tasks.getGroupForList("/test/one").getFeature());
	}
	
	@Test
	void set_recurring_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setRecurring(1, false);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Set recurring for task 1 to false\"");

		assertFalse(task.isRecurring());
	}

	@Test
	void set_list_project_adds_and_commits_list_file_to_git() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));

		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList("/test", false);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setProject(tasks.findListForTask(1), "Issue", true);
		
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
		
		tasks.addList("/test/one", false);
		tasks.setActiveList("/test/one");
		
		tasks.setProject(tasks.getGroupForList("/test/one"), "Issue", true);
		
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
		
		tasks.addList("/test", false);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		tasks.setFeature(tasks.findListForTask(1), "Feature", true);
		
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
		
		tasks.addList("/test/one", false);
		tasks.setActiveList("/test/one");
		
		tasks.setFeature(tasks.getGroupForList("/test/one"), "Feature", true);
		
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
		tasks.finishTask(1);

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setRecurring(1, true));

		assertEquals("Cannot set task 1 recurring state. The task has been finished.", taskException.getMessage());

		assertFalse(task.isRecurring());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_feature_on_finished_list() {
		tasks.addList("/one", true);
		tasks.setFeature(tasks.getListByName("/one"), "test", true);
		tasks.finishList("/one");

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setFeature(tasks.getListByName("/one"), "new-feature", true));

		assertEquals("Cannot set feature on list '/one' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getListByName("/one").getFeature());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_project_on_finished_list() {
		tasks.addList("/one", true);
		tasks.setProject(tasks.getListByName("/one"), "test", true);
		tasks.finishList("/one");

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setProject(tasks.getListByName("/one"), "new-project", true));

		assertEquals("Cannot set project on list '/one' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getListByName("/one").getProject());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_feature_on_finished_group() {
		tasks.addGroup("/one/");
		tasks.setFeature(tasks.getGroup("/one/"), "test", true);
		tasks.finishGroup("/one/");

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setFeature(tasks.getGroup("/one/"), "new-project", true));

		assertEquals("Cannot set feature on group '/one/' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getGroup("/one/").getFeature());

		Mockito.verifyNoInteractions(writer, osInterface);
	}

	@Test
	void exception_is_thrown_when_setting_project_on_finished_group() {
		tasks.addGroup("/one/");
		tasks.setProject(tasks.getGroup("/one/"), "test", true);
		tasks.finishGroup("/one/");

		Mockito.reset(writer, osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.setProject(tasks.getGroup("/one/"), "new-feature", true));

		assertEquals("Cannot set project on group '/one/' because it has been finished.", taskException.getMessage());

		assertEquals("test", tasks.getGroup("/one/").getProject());

		Mockito.verifyNoInteractions(writer, osInterface);
	}
}
