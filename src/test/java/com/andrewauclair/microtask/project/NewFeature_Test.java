// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NewFeature_Test extends ProjectBaseTestCase {
	@Test
	void throws_exception_when_feature_exists() {
		Project project = new Project(osInterface, tasks, existingGroup("/projects/micro-task/"), "micro-task");
		project.addFeature(new NewFeature(project, "test"), true);
		TaskException exception = assertThrows(TaskException.class, () -> new NewFeature(project, "test"));

		assertEquals("Feature 'test' already exists on project 'micro-task'", exception.getMessage());
	}

	@Test
	void new_feature_name() {
		Project project = new Project(osInterface, tasks, existingGroup("/projects/micro-task/"), "micro-task");

		NewFeature feature = new NewFeature(project, "test");

		assertEquals("test", feature.getName());
		assertEquals("test", feature.toString());
	}
}
