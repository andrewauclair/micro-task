// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.NewMilestone;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

@CommandLine.Command(name = "milestone")
public class AddMilestoneCommand implements Runnable {
	private final Projects projects;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The project to add the feature to.")
	private ExistingProject project;

	@CommandLine.Parameters(index = "1", description = "The milestone to add.")
	private String milestone;

	public AddMilestoneCommand(Projects projects) {
		this.projects = projects;
	}

	@Override
	public void run() {
		Project project = projects.getProject(this.project);

		project.addMilestone(new NewMilestone(project, milestone), true);

		System.out.println("Created milestone '" + milestone + "' for project '" + project.getName() + "'");
		System.out.println();
	}
}
