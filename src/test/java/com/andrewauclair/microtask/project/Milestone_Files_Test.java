// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.times;

public class Milestone_Files_Test extends ProjectBaseTestCase {
	@Test
	void write_milestone_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				""
		);
	}

	@Test
	void do_not_write_milestone_file() throws IOException {
		Mockito.reset(osInterface);

		project.addMilestone(new NewMilestone(project, "20.9.3"), false);

		Mockito.verify(osInterface, times(0)).createOutputStream(Mockito.anyString());
	}

	@Test
	void write_milestone_file_when_adding_feature_to_milestone() throws IOException {
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);
		project.addFeature(new NewFeature(project, "one"), true);
		project.addFeature(new NewFeature(project, "two"), true);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ExistingMilestone milestone = new ExistingMilestone(project, "20.9.3");

		project.getMilestone(milestone).addFeature(new ExistingFeature(project, "one"));

		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		project.getMilestone(milestone).addFeature(new ExistingFeature(project, "two"));

		project.getMilestone(milestone).save();

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				"feature one",
				"feature two",
				""
		);
	}

	@Test
	void write_milestone_file_when_adding_task_to_milestone() throws IOException {
		project.addMilestone(new NewMilestone(project, "20.9.3"), true);
		project.addFeature(new NewFeature(project, "one"), true);
		project.addFeature(new NewFeature(project, "two"), true);

		tasks.addTask("Test");
		tasks.addTask("Test");

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ExistingMilestone milestone = new ExistingMilestone(project, "20.9.3");

		project.getMilestone(milestone).addFeature(new ExistingFeature(project, "one"));
		project.getMilestone(milestone).addFeature(new ExistingFeature(project, "two"));

		project.getMilestone(milestone).addTask(existingID(1));

		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/milestone-20.9.3.txt")).thenReturn(new DataOutputStream(outputStream));

		project.getMilestone(milestone).addTask(existingID(2));

		project.getMilestone(milestone).save();

		TestUtils.assertOutput(outputStream,
				"name 20.9.3",
				"feature one",
				"feature two",
				"task 1",
				"task 2",
				""
		);
	}
}
