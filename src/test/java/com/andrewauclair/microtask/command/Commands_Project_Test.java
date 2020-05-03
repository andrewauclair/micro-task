// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

class Commands_Project_Test extends CommandsBaseTestCase {
	@Test
	void list_projects() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));

		projects.createProject(existingGroup("/test/"));
		projects.createProject(existingGroup("/one/"));
		projects.createProject(existingGroup("/two/"));

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
				"Usage:  project [-h] [--list] [--progress] [-n=<name>]",
				"  -h, --help          Show this help message.",
				"      --list",
				"  -n, --name=<name>",
				"      --progress"
		);
	}
}
