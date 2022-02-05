// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import org.junit.jupiter.api.Test;

class Commands_Project_Test extends CommandsBaseTestCase {
	@Test
	void list_projects() {
		projects.createProject(new NewProject(projects, "test"), true);
		projects.createProject(new NewProject(projects, "one"), true);
		projects.createProject(new NewProject(projects, "two"), true);

		commands.execute(printStream, "project --list");

		assertOutput(
				"test",
				"one",
				"two",
				""
		);
	}

	@Test
	void project_command_help() {
		commands.execute(printStream, "project --help");

		assertOutput(
				"Usage:  project [-hv] [--list] [--progress] [-n=<name>]",
				"  -h, --help          Show this help message.",
				"      --list",
				"  -n, --name=<name>",
				"      --progress",
				"  -v, --verbose"
		);
	}
}
