// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Commands_Start_List_Test extends CommandsBaseTestCase {
	@Test
	void start_a_list() {
		commands.execute(printStream, "start list /default");

		assertEquals(existingList("/default"), tasks.getActiveContext().getActiveList().get());
	}

	@Test
	void starting_a_list_clears_the_active_group() {
		tasks.addGroup(newGroup("/projects/"));
		tasks.getActiveContext().setActiveGroup(existingGroup("/projects/"));

		commands.execute(printStream, "start list /default");

		assertThat(tasks.getActiveContext().getActiveGroup()).isEmpty();
	}
}
