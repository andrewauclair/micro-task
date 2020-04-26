// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Commands_Project_Features_Test extends CommandsBaseTestCase {
	@Test
	void add_a_feature_to_active_project() {
		projects.createProject("test");
		projects.setActiveProject("test");

		commands.execute(System.out, "project --name test --add-feature one");

		Project test = projects.getProject("test");

		assertTrue(test.hasFeature("one"));

		assertOutput(
				"Added feature 'one' to project 'test'",
				""
		);
	}

	@Test
	@Disabled
	void add_sub_feature() {
		projects.createProject("test");
		projects.setActiveProject("test");
		projects.getProject("test").addFeature("one", null);

		commands.execute(System.out, "project --name test --add-feature two --parent-feature one");

		assertTrue(projects.getProject("test").hasFeature("one/two"));

		assertOutput(
				"Added feature 'one/two' to project 'test'",
				""
		);
	}
}
