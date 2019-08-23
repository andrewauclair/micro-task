// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class Tasks_Set_Test extends TaskBaseTestCase {
	@Test
	void set_issue_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);
		
		Task task = tasks.setIssue(1, 12345);
		
		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}
	
	@Test
	void set_project_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setProject(1, "Issue");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}

	@Test
	void set_feature_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);

		Task task = tasks.setFeature(1, "Feature");

		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}

	@Test
	void set_issue_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);
		
		tasks.setIssue(1, 12345);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Set issue for task 1 to 12345\"");
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
