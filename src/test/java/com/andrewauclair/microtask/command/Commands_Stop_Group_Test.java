// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Stop_Group_Test extends CommandsBaseTestCase {
	@Test
	void stop_the_active_group() {
		tasks.getActiveContext().setActiveGroup(existingGroup("/"));

		commands.execute(printStream, "stop group");

		assertThat(tasks.getActiveContext().getActiveGroup()).isEmpty();
	}
}
