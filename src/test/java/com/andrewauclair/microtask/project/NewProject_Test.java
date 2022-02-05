// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NewProject_Test extends ProjectBaseTestCase {
	@Test
	void throws_exception_when_project_already_exists() {
		TaskException exception = assertThrows(TaskException.class, () -> new NewProject(projects, "micro-task"));

		assertEquals("Project 'micro-task' already exists.", exception.getMessage());
	}

	@Test
	void new_project_name() {
		NewProject project = new NewProject(projects, "test");

		assertEquals("test", project.getName());
		assertEquals("test", project.toString());
	}
}
