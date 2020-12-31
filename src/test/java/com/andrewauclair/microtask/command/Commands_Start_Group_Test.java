// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Commands_Start_Group_Test extends CommandsBaseTestCase {
	@Test
	void start_a_group() {
		tasks.addGroup(newGroup("/projects/"));

		commands.execute(printStream, "start group /projects/");

		assertThat(tasks.getActiveContext().getActiveGroup()).isPresent();
		assertEquals(existingGroup("/projects/"), tasks.getActiveContext().getActiveGroup().get());
	}

	@Test
	void starting_a_group_sets_the_active_list_to_none() {
		tasks.getActiveContext().setActiveList(existingList("/default"));

		tasks.addGroup(newGroup("/projects/"));

		commands.execute(printStream, "start group /projects/");

		assertThat(tasks.getActiveContext().getActiveList()).isEmpty();
	}
}
