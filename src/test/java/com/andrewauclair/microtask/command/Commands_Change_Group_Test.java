// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Change_Group_Test extends CommandsBaseTestCase {
	@Test
	void switch_group_command() {
		tasks.createGroup(newGroup("/test/one/two/three/"));

		commands.execute(printStream, "ch -g /test/one/two/three/");

		assertEquals("/test/one/two/three/", tasks.getCurrentGroup().getFullPath());
	}

	@Test
	void switch_to_relative_group() {
		tasks.createGroup(newGroup("/one/two/three/"));
		tasks.setCurrentGroup(existingGroup("/one/two/"));

		commands.execute(printStream, "ch -g three/");

		assertOutput(
				"Switched to group '/one/two/three/'",
				""
		);
	}

	@Test
	void set_active_group_in_local_settings_when_changing_groups() {
		tasks.addGroup(newGroup("/test/"));

		commands.execute(printStream, "ch -g test/");

		Mockito.verify(localSettings).setActiveGroup(existingGroup("/test/"));
	}

	@Test
	void invalid_group_path() {
		commands.execute(printStream, "ch -g /projects/test");

		assertOutput(
				"Invalid value for option '--group': Group name must end in /",
				""
		);
	}
}
