// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class Tasks_Finish_Test extends TaskBaseTestCase {
	@Test
	void finish_writes_file_on_correct_list() {
		tasks.addTask("Test");
		tasks.addList("one");

		tasks.startTask(1, false);
		
		tasks.setCurrentList("one");
		
		tasks.addTask("Test 2");
		
		Mockito.reset(writer);
		
		Task task = tasks.finishTask();
		
		Mockito.verify(writer).writeTask(task, "git-data/tasks/default/1.txt");
	}
	
	@Test
	void finish_tells_git_control_to_add_correct_files() {
		tasks.addTask("Test");
		tasks.addList("one");

		tasks.startTask(1, false);
		
		tasks.setCurrentList("one");
		
		tasks.addTask("Test 2");
		
		Mockito.reset(osInterface);
		
		tasks.finishTask();
		
		InOrder order = Mockito.inOrder(osInterface);
		
		order.verify(osInterface).runGitCommand("git add tasks/default/1.txt");
		order.verify(osInterface).runGitCommand("git commit -m \"Finished task 1 - 'Test'\"");
	}
}
