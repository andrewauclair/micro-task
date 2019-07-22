// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

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
	void set_charge_writes_file() {
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);
		
		Task task = tasks.setCharge(1, "Issue");
		
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
	void set_charge_adds_and_commits_file_to_git() {
		InOrder order = Mockito.inOrder(osInterface);
		
		tasks.addTask("Test 1");
		Mockito.reset(osInterface);
		
		tasks.setCharge(1, "Issue");
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Set charge for task 1 to 'Issue'\"");
	}
}
