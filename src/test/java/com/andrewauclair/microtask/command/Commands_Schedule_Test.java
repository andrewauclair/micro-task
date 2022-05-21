// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
	void print_schedule() {
		projects.createProject(new NewProject(projects, "test"), false);

		commands.execute(printStream, "schedule --project test --pct 20");

		commands.execute(printStream, "schedule");

		assertOutput(
				"Schedule",
				"",
				" 20 %  test"
		);
	}

	@Test
	void create_for_day() {
		projects.createProject(new NewProject(projects, "proj-1"), true);
		projects.createProject(new NewProject(projects, "proj-2"), true);
		projects.createProject(new NewProject(projects, "proj-3"), true);

		commands.execute(printStream, "schedule --project proj-1 --pct 35");
		commands.execute(printStream, "schedule --project proj-2 --pct 25");
		commands.execute(printStream, "schedule --project proj-3 --pct 15");
		// leaves 25% unallocated which will pull from lowest numbered tasks not in scheduled projects

		for (int i = 0; i < 15; i++) {
			tasks.addTask("Test", new ExistingListName(tasks, "/default"));
			tasks.addTask("Test", new ExistingListName(tasks, "/projects/proj-1/general"));
			tasks.addTask("Test", new ExistingListName(tasks, "/projects/proj-2/general"));
			tasks.addTask("Test", new ExistingListName(tasks, "/projects/proj-3/general"));
		}
		assertThat(schedule.tasks()).isEmpty();

		commands.execute(printStream, "schedule --day");

		assertThat(schedule.tasks().stream()
				.map(task -> task.ID())
				.collect(Collectors.toList())
		).containsOnly(
				4L, 8L, 7L, 11L, 12L, 16L, 17L, 6L, 10L, 14L, 18L, 5L, 9L, 13L, 21L, 25L
		);
	}

	@Test
	void schedule_command_help() {
		commands.execute(printStream, "schedule --help");

		assertOutput(
				"Usage:  schedule [-h] [--day] [--pct=<percent>] [--project=<project>]",
						"Schedule projects.",
						"      --day",
						"  -h, --help                Show this help message.",
						"      --pct=<percent>",
						"      --project=<project>"
		);
	}
}
