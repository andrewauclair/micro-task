// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Stop_Tags_Test extends CommandsBaseTestCase {
	@Test
	void stop_a_single_tag() {
		tasks.getActiveContext().setActiveTags(Arrays.asList("one", "two"));

		commands.execute(printStream, "stop tags one");

		assertThat(tasks.getActiveContext().getActiveTags()).containsOnly("two");
	}

	@Test
	void stop_all_tags() {
		tasks.getActiveContext().setActiveTags(Arrays.asList("one", "two"));

		commands.execute(printStream, "stop tags");

		assertThat(tasks.getActiveContext().getActiveTags()).isEmpty();
	}
}
