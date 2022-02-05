// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.schedule;

import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.ProjectBaseTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Schedule_Files_Test extends ProjectBaseTestCase {
	@Test
	void schedule_is_saved_to_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


		Project one = projects.createProject(new NewProject(projects, "one"), true);
		Project two = projects.createProject(new NewProject(projects, "two"), true);

		Schedule schedule = new Schedule(tasks, osInterface);

		schedule.scheduleProject(one, 75);

		Mockito.when(osInterface.createOutputStream("git-data/schedule.txt")).thenReturn(new DataOutputStream(outputStream));

		schedule.scheduleProject(two, 10);

		TestUtils.assertOutput(outputStream,
				"one 75",
				"two 10",
				""
		);
	}
}
