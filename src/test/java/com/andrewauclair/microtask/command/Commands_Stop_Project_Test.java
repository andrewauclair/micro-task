// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Stop_Project_Test extends CommandsBaseTestCase {
	@Test
	void stop_the_active_project() {
		projects.createProject(new NewProject(projects, "micro-task"), true);

		tasks.getActiveContext().setActiveProject(new ExistingProject(projects, "micro-task"));

		commands.execute(printStream, "stop project");

		assertThat(tasks.getActiveContext().getActiveProject()).isEmpty();
	}
}
