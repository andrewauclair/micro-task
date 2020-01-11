// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

// TODO I would like a more detailed broken down set of tests for the times command variations
class Commands_Times_List_Test extends CommandsBaseTestCase {
	@Test
	void times_on_default_list() {
		addTaskWithTimes("Task 1", 1000, 2000);
		addTaskTimes(1, 4000, 4500);
		
		addTaskWithTimes("Task 2", 2000, 2800);
		addTaskTimes(2, 3500, 4600);
		
		tasks.addList("test");
		tasks.setActiveList("test");
		
		addTaskWithTimes("Task 3", 1000, 3000);
		addTaskTimes(3, 3800, 4700);
		
		commands.execute(printStream, "times --list default");
		
		assertOutput(
				"Times for list 'default'",
				"",
				"Total time spent on list: 56m 40s",
				""
		);
	}
}
