// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Projects_Features_Test extends ProjectBaseTestCase {
	@Test
	void add_feature_to_parent_feature() {
		project.addFeature("one", null);
		project.addFeature("two", "one");

		assertTrue(project.hasFeature("one/two"));
	}
}
