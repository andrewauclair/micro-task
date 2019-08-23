// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

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
	void set_project_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setProject(1, "Project");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");

		assertEquals("Project", task.getProject());
	}

	@Test
	void set_feature_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setFeature(1, "Feature");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");

		assertEquals("Feature", task.getFeature());
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
	void set_project_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		tasks.setProject(1, "Issue");
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Set project for task 1 to 'Issue'\"");
	}

	@Test
	void set_feature_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		tasks.setFeature(1, "Feature");

		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Set feature for task 1 to 'Feature'\"");
	}
}
