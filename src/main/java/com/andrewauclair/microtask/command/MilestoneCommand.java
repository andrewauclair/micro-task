// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.command.project.MilestoneFeatureCommand;
import com.andrewauclair.microtask.command.project.MilestoneTaskCommand;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Projects;
import picocli.CommandLine;

@CommandLine.Command(name = "milestone", subcommands = {
		MilestoneFeatureCommand.class,
		MilestoneTaskCommand.class
})
public class MilestoneCommand implements Runnable {
	public final Projects projects;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The project.")
	public ExistingProject project;

	@CommandLine.Parameters(index = "1", description = "The milestone.")
	public String milestone;

	public MilestoneCommand(Projects projects) {
		this.projects = projects;
	}

	@Override
	public void run() {

	}
}
