// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.project.ExistingMilestone;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "milestone")
public class StartMilestoneCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;

	@CommandLine.Parameters(index = "0", description = "The project.")
	private ExistingProject project;

	@CommandLine.Parameters(index = "1", description = "The milestone to start.")
	private String milestone;

	public StartMilestoneCommand(Tasks tasks, Projects projects) {
		this.tasks = tasks;
		this.projects = projects;
	}

	@Override
	public void run() {
		tasks.getActiveContext().setActiveMilestone(new ExistingMilestone(projects.getProject(project), milestone));
	}
}
