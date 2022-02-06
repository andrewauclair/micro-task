// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Commands_Change_Project_Test extends CommandsBaseTestCase {
	@Test
	void change_the_active_project() {
		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "ch -p test");

		assertEquals("test", projects.getActiveProject().getName());

		assertOutput(
				"Switched to project 'test'",
				""
		);
	}
}
