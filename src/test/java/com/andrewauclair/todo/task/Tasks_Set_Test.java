// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TestUtils;
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
		tasks.addList("/test");
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setProject(tasks.findListForTask(1), "Project");

		assertEquals("Project", tasks.findListForTask(1).getProject());
	}
	
	@Test
	void set_group_project() {
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		
		tasks.setProject(tasks.getGroupForList("/test/one"), "Project");
		
		assertEquals("Project", tasks.getGroupForList("/test/one").getProject());
	}
	
	@Test
	void set_list_feature() {
		tasks.addList("/test");
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setFeature(tasks.findListForTask(1), "Feature");

		assertEquals("Feature", tasks.findListForTask(1).getFeature());
	}
	
	@Test
	void set_group_feature() {
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		
		tasks.setFeature(tasks.getGroupForList("/test/one"), "Feature");
		
		assertEquals("Feature", tasks.getGroupForList("/test/one").getFeature());
	}
	
	@Test
	void set_recurring_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setRecurring(1, false);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set recurring for task 1 to false\"", false);

		assertFalse(task.isRecurring());
	}

	@Test
	void set_list_project_adds_and_commits_list_file_to_git() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));

		InOrder order = Mockito.inOrder(osInterface);

		tasks.addList("/test");
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setProject(tasks.findListForTask(1), "Issue");
		
		TestUtils.assertOutput(listStream,
				"Issue",
				"",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/list.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set project for list '/test' to 'Issue'\"", false);
	}
	
	@Test
	void set_group_project_adds_and_commits_group_file_to_git() throws IOException {
		OutputStream groupStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(groupStream));
		
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		
		tasks.setProject(tasks.getGroupForList("/test/one"), "Issue");
		
		TestUtils.assertOutput(groupStream,
				"Issue",
				"",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set project for group '/test/' to 'Issue'\"", false);
	}
	
	@Test
	void set_list_feature_adds_and_commits_file_to_git() throws IOException {
		OutputStream listStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(listStream));
		
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addList("/test");
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setFeature(tasks.findListForTask(1), "Feature");
		
		TestUtils.assertOutput(listStream,
				"",
				"Feature",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/list.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set feature for list '/test' to 'Feature'\"", false);
	}
	
	@Test
	void set_group_feature_adds_and_commits_file_to_git() throws IOException {
		OutputStream groupStream = new ByteArrayOutputStream();
		
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(groupStream));
		
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		
		tasks.setFeature(tasks.getGroupForList("/test/one"), "Feature");
		
		TestUtils.assertOutput(groupStream,
				"",
				"Feature",
				""
		);
		
		order.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Set feature for group '/test/' to 'Feature'\"", false);
	}
}
