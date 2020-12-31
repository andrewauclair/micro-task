// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Project_Create_Test extends ProjectBaseTestCase {
	@Test
	void create_a_project() {
		assertTrue(projects.hasProject("micro-task"));
	}

	@Test
	void get_the_new_project_by_name() {
		assertEquals("micro-task", project.getName());
	}
}
