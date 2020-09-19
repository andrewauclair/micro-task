// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Stop_Milestone_Test extends CommandsBaseTestCase {
	@Test
	void stop_the_active_milestone() {
		projects.createProject(new NewProject(projects, "micro-task"), true);
		Project project = projects.getProject(new ExistingProject(projects, "micro-task"));

		project.addMilestone(new NewMilestone(project, "20.9.3"), true);

		tasks.getActiveContext().setActiveMilestone(new ExistingMilestone(project, "20.9.3"));

		commands.execute(printStream, "stop milestone");

		assertThat(tasks.getActiveContext().getActiveMilestone()).isEmpty();
	}
}
