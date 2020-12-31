// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.ExistingFeature;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.task.TaskListFinder;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Add_Feature_Test extends CommandsBaseTestCase {
	@Test
	void make_a_new_feature_from_an_existing_group() {
		tasks.createGroup(newGroup("/projects/test/feature/"));

		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "add feature test feature");

		Project project = projects.getProject(new ExistingProject(projects, "test"));
		assertTrue(project.hasFeature("feature"));

		assertOutput(
				"Created feature 'feature' for project 'test'",
				""
		);
	}

	@Test
	void make_a_new_feature_from_an_existing_list() {
		tasks.createGroup(newGroup("/projects/test/"));
		tasks.addList(newList("/projects/test/feature"), true);

		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "add feature test feature");

		Project project = projects.getProject(new ExistingProject(projects, "test"));
		assertTrue(project.hasFeature("feature"));

		assertOutput(
				"Created feature 'feature' for project 'test'",
				""
		);
	}

	@Test
	void new_feature_creates_list_when_no_list_or_group_exists() {
		projects.createProject(new NewProject(projects, "test"), true);

		commands.execute(System.out, "add feature test feature");

		Project project = projects.getProject(new ExistingProject(projects, "test"));
		assertTrue(project.hasFeature("feature"));

		TaskListFinder finder = new TaskListFinder(tasks);

		assertTrue(finder.hasList(new ExistingListName(tasks, "/projects/test/feature")));

		assertOutput(
				"Created feature 'feature' for project 'test'",
				""
		);
	}
}
