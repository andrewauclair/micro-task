// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Commands_Start_Feature_Test extends CommandsBaseTestCase {
	@Test
	void start_a_feature() {
		projects.createProject(new NewProject(projects, "micro-task"), true);

		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));
		project.addFeature(new NewFeature(project, "times"), true);

		commands.execute(System.out, "start feature micro-task times");

		assertThat(tasks.getActiveContext().getActiveFeature()).isPresent();
		assertEquals(ExistingFeature.tryCreate(project, "times"), tasks.getActiveContext().getActiveFeature().get());
	}
}
