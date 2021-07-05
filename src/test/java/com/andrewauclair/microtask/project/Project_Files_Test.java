// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Project_Files_Test extends ProjectBaseTestCase {
	@Test
	void projects_are_saved_to_a_file() throws IOException {
		ByteArrayOutputStream outputStreamProject = new ByteArrayOutputStream();
		ByteArrayOutputStream outputStreamGroup = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/done/project.txt")).thenReturn(new DataOutputStream(outputStreamProject));
		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/done/group.txt")).thenReturn(new DataOutputStream(outputStreamGroup));

		projects.createProject(new NewProject(projects, "done"), true);

		TestUtils.assertOutput(outputStreamProject,
				"name done",
				""
		);

		TestUtils.assertOutput(outputStreamGroup,
				"InProgress",
				""
		);
	}

	@Test
	void load_projects_from_file() throws IOException {
		tasks.createGroup(newGroup("/projects/one/"));
		tasks.createGroup(newGroup("/projects/two/"));

		Mockito.when(osInterface.fileExists("git-data/tasks/projects/one/project.txt")).thenReturn(true);
		Mockito.when(osInterface.fileExists("git-data/tasks/projects/two/project.txt")).thenReturn(true);
		Mockito.when(osInterface.createInputStream("git-data/tasks/projects/one/project.txt")).thenReturn(
				createInputStream("name one")
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/projects/two/project.txt")).thenReturn(
				createInputStream("name two")
		);

		projects.load();

		assertTrue(projects.hasProject("one"));
		assertTrue(projects.hasProject("two"));
	}
}
