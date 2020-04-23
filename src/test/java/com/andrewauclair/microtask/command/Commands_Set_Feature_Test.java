// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskFinder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_Feature_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_feature_command_for_list() {
		tasks.addList(newList("/test"), true);
		tasks.setActiveList(existingList("/test"));
		tasks.addTask("Test 1");

		commands.execute(printStream, "set list /test --feature \"Feature\"");

		assertEquals("Feature", new TaskFinder(tasks).getFeatureForTask(existingID(1)));

		assertOutput(
				"Set feature for list '/test' to 'Feature'",
				""
		);
	}

	@Test
	void execute_set_feature_command_for_group() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setActiveList(existingList("/test/one"));
		tasks.addTask("Test 1");

		commands.execute(printStream, "set group /test/ --feature \"Feature\"");

		assertEquals("Feature", new TaskFinder(tasks).getFeatureForTask(existingID(1)));

		assertOutput(
				"Set feature for group '/test/' to 'Feature'",
				""
		);
	}
}
