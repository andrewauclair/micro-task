// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExistingFeature_Test extends ProjectBaseTestCase {
	@Test
	void throws_exception_when_feature_does_not_exist() {
		Project project = new Project(osInterface, tasks, existingGroup("/projects/micro-task/"), "micro-task");

		TaskException exception = assertThrows(TaskException.class, () -> new ExistingFeature(project, "test"));

		assertEquals("Feature 'test' does not exist on project 'micro-task'", exception.getMessage());
	}

	@Test
	void existing_feature_name() {
		Project project = new Project(osInterface, tasks, existingGroup("/projects/micro-task/"), "micro-task");
		project.addFeature(new NewFeature(project, "test"), true);

		ExistingFeature feature = new ExistingFeature(project, "test");

		assertEquals("test", feature.getName());
		assertEquals("test", feature.toString());
	}
}
