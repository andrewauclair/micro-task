// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Start_Tags_Test extends CommandsBaseTestCase {
	@Test
	void start_tags() {
		commands.execute(printStream, "start tags design phase-1");

		assertThat(tasks.getActiveContext().getActiveTags()).containsOnly("design", "phase-1");

		assertOutput(
				"Starting tag(s): design, phase-1",
				""
		);
	}
}
