// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewProject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Start_Project_Test extends CommandsBaseTestCase {
	@Test
	void start_a_project() {
		projects.createProject(new NewProject(projects, "micro-task"), true);

		commands.execute(printStream, "start project micro-task");

		assertThat(tasks.getActiveContext().getActiveProject()).isNotEmpty();
		assertThat(tasks.getActiveContext().getActiveProject().get()).isEqualTo(new ExistingProject(projects, "micro-task"));
	}

	@Test
	void starting_a_project_clears_the_active_list() {
		tasks.getActiveContext().setActiveList(existingList("/default"));

		projects.createProject(new NewProject(projects, "micro-task"), true);
		commands.execute(printStream, "start project micro-task");

		assertThat(tasks.getActiveContext().getActiveList()).isEmpty();
	}

	@Test
	void starting_a_project_clears_the_active_group() {
		tasks.addGroup(newGroup("/projects/"));
		tasks.getActiveContext().setActiveGroup(existingGroup("/projects/"));

		projects.createProject(new NewProject(projects, "micro-task"), true);
		commands.execute(printStream, "start project micro-task");

		assertThat(tasks.getActiveContext().getActiveGroup()).isEmpty();
	}
}
