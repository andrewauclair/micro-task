// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Start_Milestone_Test extends CommandsBaseTestCase {
	@Test
	void start_a_milestone() {
		projects.createProject(new NewProject(projects, "micro-task"), true);

		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));

		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		commands.execute(printStream, "start milestone micro-task 20.9.3");

		assertThat(tasks.getActiveContext().getActiveMilestone()).isNotEmpty();
		assertThat(tasks.getActiveContext().getActiveMilestone().get()).isEqualTo(new ExistingMilestone(project, "20.9.3"));
	}
}
