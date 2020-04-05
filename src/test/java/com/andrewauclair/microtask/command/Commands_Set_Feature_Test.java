// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_Feature_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_feature_command_for_list() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-list --list /test --feature \"Feature\"");

		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set feature for list '/test' to 'Feature'",
				""
		);
	}

	@Test
	void execute_set_feature_command_for_group() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-group --group /test/ --feature \"Feature\"");

		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set feature for group '/test/' to 'Feature'",
				""
		);
	}
}
