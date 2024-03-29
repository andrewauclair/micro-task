// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Stop_Feature_Test extends CommandsBaseTestCase {
	@Test
	void stop_the_active_feature() {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));

		project.addFeature(new NewFeature(project, "one"), true);

		tasks.getActiveContext().setActiveFeature(ExistingFeature.tryCreate(project, "one"));

		commands.execute(printStream, "stop feature");

		assertThat(tasks.getActiveContext().getActiveFeature()).isEmpty();
	}
}
