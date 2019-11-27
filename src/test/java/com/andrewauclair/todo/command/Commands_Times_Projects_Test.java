// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

class Commands_Times_Projects_Test extends CommandsBaseTestCase {
	@Test
	void basic_times_command_for_projects_and_features() {
		tasks.setActiveList("/default");
		tasks.setProject(tasks.getGroup("/"), "Project 1");
		tasks.setFeature(tasks.getGroup("/"), "Feature 1");
		
		tasks.addTask("Test");
		addTaskTimes(1, 1561080202, 1561081202);
		
		tasks.addList("one");
		tasks.setProject(tasks.getListByName("/one"), "Project 2");
		tasks.setFeature(tasks.getListByName("/one"), "Feature 2");
		
		tasks.setActiveList("/one");
		tasks.addTask("Test");
		
		addTaskTimes(2, 1561080202, 1561081402);
		
		commands.execute(printStream, "times --proj-feat");
		
		assertOutput(
				"Project 1 / Feature 1     16m 40s",
				"Project 2 / Feature 2     20m 00s",
				""
		);
	}
	
	@Test
	void tasks_with_no_project_or_feature_say_none() {
		tasks.setActiveList("/default");
		
		tasks.addTask("Test 1");
		addTaskTimes(1, 1561080202, 1561081202);
		
		commands.execute(printStream, "times --proj-feat");
		
		assertOutput(
				"None / None     16m 40s",
				""
		);
	}
	
	@Test
	void task_with_no_project_says_none() {
		tasks.setActiveList("/default");
		tasks.setFeature(tasks.getGroup("/"), "Feature 1");
		
		tasks.addTask("Test 1");
		addTaskTimes(1, 1561080202, 1561081202);
		
		commands.execute(printStream, "times --proj-feat");
		
		assertOutput(
				"None / Feature 1     16m 40s",
				""
		);
	}
	
	@Test
	void task_with_no_feature_says_none() {
		tasks.setActiveList("/default");
		tasks.setProject(tasks.getGroup("/"), "Project 1");
		
		tasks.addTask("Test 1");
		addTaskTimes(1, 1561080202, 1561081202);
		
		commands.execute(printStream, "times --proj-feat");
		
		assertOutput(
				"Project 1 / None     16m 40s",
				""
		);
	}
}
