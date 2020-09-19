// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.jline.ProjectCompleter;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

@CommandLine.Command(name = "project")
public class AddProjectCommand implements Runnable {
	private final Projects projects;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", completionCandidates = ProjectCompleter.class, description = "Make a project from a group.")
	private NewProject project;

	public AddProjectCommand(Projects projects) {
		this.projects = projects;
	}

	@Override
	public void run() {
		Project project = projects.createProject(this.project, true);

		System.out.println("Created project '" + project.getName() + "'");

		System.out.println();
	}
}
