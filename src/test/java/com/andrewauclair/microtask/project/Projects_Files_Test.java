// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Projects_Files_Test extends ProjectBaseTestCase {
	@Test
	void projects_are_saved_to_a_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/projects.txt")).thenReturn(new DataOutputStream(outputStream));

		projects.createProject(existingGroup("test/"));

		TestUtils.assertOutput(outputStream,
				"/test/",
				""
		);
	}

	@Test
	void load_projects_from_file() throws IOException {
		tasks.createGroup(newGroup("/one/"));
		tasks.createGroup(newGroup("/two/"));

		Mockito.when(osInterface.createInputStream("git-data/projects.txt")).thenReturn(
				createInputStream("/test/", "/one/", "/two/")
		);

		projects.load();

		assertTrue(projects.hasProject("test"));
		assertTrue(projects.hasProject("one"));
		assertTrue(projects.hasProject("two"));
	}
}
