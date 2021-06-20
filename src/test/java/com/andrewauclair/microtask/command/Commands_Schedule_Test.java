// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Commands_Schedule_Test extends CommandsBaseTestCase {
	@Test
	void schedule_a_project() {
		projects.createProject(new NewProject(projects, "test"), false);

		commands.execute(printStream, "schedule --project test --pct 20");

		assertTrue(schedule.hasProject("test"));
		assertEquals(20, schedule.projectPercent("test"));
	}

	@Test
	void schedule_command_help() {
		commands.execute(printStream, "schedule --help");

		assertOutput(
				"Usage:  schedule [-h] [--pct=<percent>] [--project=<project>]",
				"Schedule projects.",
				"  -h, --help                Show this help message.",
				"      --pct=<percent>",
				"      --project=<project>"
		);
	}
}
