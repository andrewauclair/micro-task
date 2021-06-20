// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.schedule.Schedule;
import picocli.CommandLine;

@CommandLine.Command(name = "schedule", description = "Schedule projects.")
public class ScheduleCommand implements Runnable {
	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Option(names = {"--project"})
	private ExistingProject project;

	@CommandLine.Option(names = {"--pct"})
	private int percent;

	private final Schedule schedule;
	private final Projects projects;

	public ScheduleCommand(Schedule schedule, Projects projects) {
		this.schedule = schedule;
		this.projects = projects;
	}

	@Override
	public void run() {
		schedule.scheduleProject(projects.getProject(project), percent);
	}
}
