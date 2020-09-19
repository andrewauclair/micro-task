// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Stop_List_Test extends CommandsBaseTestCase {
	@Test
	void stop_the_active_list() {
		tasks.getActiveContext().setActiveList(existingList("/default"));

		commands.execute(printStream, "stop list");

		assertThat(tasks.getActiveContext().getActiveList()).isEmpty();
	}
}
