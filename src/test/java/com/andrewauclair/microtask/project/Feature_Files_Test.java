// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.project;

import com.andrewauclair.microtask.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Feature_Files_Test extends ProjectBaseTestCase {
	@Test
	void write_feature_file() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/micro-task/one/feature.txt")).thenReturn(new DataOutputStream(outputStream));

		project.addFeature(new NewFeature(project, "one"), true);

		TestUtils.assertOutput(outputStream,
				"name one",
				""
		);
	}
}
