// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Commands_Change_Project_Test extends CommandsBaseTestCase {
	@Test
	void change_the_active_project() {
		tasks.addGroup(newGroup("test/"));

		projects.createProject(existingGroup("test/"));

		commands.execute(System.out, "ch -p test");

		assertEquals("test", projects.getActiveProject().getName());

		assertOutput(
				"Switched to project 'test'",
				""
		);
	}
}
