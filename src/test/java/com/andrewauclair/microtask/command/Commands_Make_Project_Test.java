// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Commands_Make_Project_Test extends CommandsBaseTestCase {
	@Test
	void make_new_project() {
		tasks.createGroup(newGroup("/test/"));

		commands.execute(System.out, "mk -p test/");

		assertTrue(projects.hasProject("test"));

		assertOutput(
				"Created project 'test'",
				""
		);
	}
}
