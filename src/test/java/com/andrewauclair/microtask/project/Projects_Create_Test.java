// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Projects_Create_Test extends ProjectBaseTestCase {
	@Test
	void create_a_plan() {
		projects.createProject("Test");

		assertTrue(projects.hasProject("Test"));
	}

	@Test
	void get_the_new_plan_by_name() {
		projects.createProject("Test");

		Project project = projects.getProject("Test");

		assertEquals("Test", project.getName());
	}
}
