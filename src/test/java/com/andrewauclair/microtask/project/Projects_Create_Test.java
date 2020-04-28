// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Projects_Create_Test extends ProjectBaseTestCase {
	@Test
	void create_a_plan() {
		projects.createProject(existingGroup("test/"));

		assertTrue(projects.hasProject("test"));
	}

	@Test
	void get_the_new_plan_by_name() {
		projects.createProject(existingGroup("test/"));

		Project project = projects.getProject("test");

		assertEquals("test", project.getName());
	}
}
