// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_recurring_true_command() {
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --task 1 --recurring true");
		
		Task task = tasks.getTask(1);
		
		assertTrue(task.isRecurring());
	}

	@Test
	void execute_set_recurring_false_command() {
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --task 1 --recurring false");
		
		Task task = tasks.getTask(1);
		
		assertFalse(task.isRecurring());
	}

	@Test
	void execute_set_project_command_for_list() {
		tasks.addList("/test");
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --list /test --project \"Issues\"");
		
		assertEquals("Issues", tasks.getProjectForTask(1));
	}
	
	@Test
	void execute_set_project_command_for_group() {
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --group /test/ --project \"Issues\"");
		
		assertEquals("Issues", tasks.getProjectForTask(1));
	}
	
	@Test
	void execute_set_feature_command_for_list() {
		tasks.addList("/test");
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --list /test --feature \"Feature\"");
		
		assertEquals("Feature", tasks.getFeatureForTask(1));
	}
	
	@Test
	void execute_set_feature_command_for_group() {
		tasks.addList("/test/one");
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");
		
		commands.execute(printStream, "set --group /test/ --feature \"Feature\"");
		
		assertEquals("Feature", tasks.getFeatureForTask(1));
	}
}
