// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_Project_Feature_Test extends CommandsBaseTestCase {
	@Test
	void set_project_and_feature_for_list_at_same_time() {
		tasks.addList("/test", true);
		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-list --list /test --project \"Issues\" --feature \"Feature\"");

		assertEquals("Issues", tasks.getProjectForTask(1));
		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set project for list '/test' to 'Issues'",
				"Set feature for list '/test' to 'Feature'",
				""
		);
	}

	@Test
	void set_project_and_feature_for_group_at_same_time() {
		tasks.addList("/test/one", true);
		tasks.setActiveList("/test/one");
		tasks.addTask("Test 1");

		commands.execute(printStream, "set-group --group /test/ --project \"Issues\" --feature \"Feature\"");

		assertEquals("Issues", tasks.getProjectForTask(1));
		assertEquals("Feature", tasks.getFeatureForTask(1));

		assertOutput(
				"Set project for group '/test/' to 'Issues'",
				"Set feature for group '/test/' to 'Feature'",
				""
		);
	}
}
