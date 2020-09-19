// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.project.ExistingFeature;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "feature")
public class StartFeatureCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The project.")
	private ExistingProject project;

	@CommandLine.Parameters(index = "1", description = "The feature to start.")
	private String feature;

	public StartFeatureCommand(Tasks tasks, Projects projects) {
		this.tasks = tasks;
		this.projects = projects;
	}

	@Override
	public void run() {
		Project project = projects.getProject(this.project);
		tasks.getActiveContext().setActiveFeature(new ExistingFeature(project, feature));
	}
}
