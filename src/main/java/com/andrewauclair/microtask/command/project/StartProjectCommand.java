// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "project")
public class StartProjectCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The project to start.")
	private ExistingProject project;

	public StartProjectCommand(Tasks tasks, Projects projects) {
		this.tasks = tasks;
		this.projects = projects;
	}

	@Override
	public void run() {
		tasks.getActiveContext().setNoActiveList();
		tasks.getActiveContext().setNoActiveGroup();
		tasks.getActiveContext().setActiveProject(project);
	}
}
