// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.project.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Milestone_Test extends CommandsBaseTestCase {
	@Test
	void add_a_feature_to_milestone() throws IOException {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		project.addFeature(new NewFeature(project, "one"), true);
		project.addFeature(new NewFeature(project, "two"), true);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "milestone micro-task 20.9.3 feature add one two");

		Milestone milestone = project.getMilestone(new ExistingMilestone(project, "20.9.3"));

		assertThat(milestone.getFeatures()).containsOnly(
				ExistingFeature.tryCreate(project, "one"),
				ExistingFeature.tryCreate(project, "two")
		);

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				"feature one",
				"feature two",
				""
		);
	}

	@Test
	void remove_a_feature_from_milestone() throws IOException {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		project.addFeature(new NewFeature(project, "one"), true);
		project.addFeature(new NewFeature(project, "two"), true);
		project.addFeature(new NewFeature(project, "three"), true);

		Milestone milestone = project.getMilestone(new ExistingMilestone(project, "20.9.3"));

		milestone.addFeature(ExistingFeature.tryCreate(project, "one"));
		milestone.addFeature(ExistingFeature.tryCreate(project, "two"));
		milestone.addFeature(ExistingFeature.tryCreate(project, "three"));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "milestone micro-task 20.9.3 feature remove two three");

		assertThat(milestone.getFeatures()).containsOnly(
				ExistingFeature.tryCreate(project, "one")
		);

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				"feature one",
				""
		);
	}

	@Test
	void add_tasks_to_milestone() throws IOException {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		Milestone milestone = project.getMilestone(new ExistingMilestone(project, "20.9.3"));

		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "milestone micro-task 20.9.3 task add 1 2 3");

		assertThat(milestone.getTasks()).containsOnly(existingID(1), existingID(2), existingID(3));

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				"task 1",
				"task 2",
				"task 3",
				""
		);
	}

	@Test
	void remove_tasks_from_milestone() throws IOException {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		Milestone milestone = project.getMilestone(new ExistingMilestone(project, "20.9.3"));

		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		milestone.addTask(existingID(1));
		milestone.addTask(existingID(2));
		milestone.addTask(existingID(3));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "milestone micro-task 20.9.3 task remove 1 2");

		assertThat(milestone.getTasks()).containsOnly(existingID(3));

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				"task 3",
				""
		);
	}
}
