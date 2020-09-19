// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.command.MilestoneCommand;
import com.andrewauclair.microtask.project.ExistingMilestone;
import com.andrewauclair.microtask.project.Milestone;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.task.ExistingID;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "task", subcommands = {
		MilestoneTaskCommand.MilestoneTaskAddCommand.class,
		MilestoneTaskCommand.MilestoneTaskRemoveCommand.class
})
public class MilestoneTaskCommand implements Runnable {
	@CommandLine.ParentCommand
	public MilestoneCommand parent;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Spec
	private CommandLine.Model.CommandSpec spec;

	@Override
	public void run() {
		spec.commandLine().usage(System.out);
	}

	@CommandLine.Command(name = "add")
	public static class MilestoneTaskAddCommand implements Runnable {
		@CommandLine.ParentCommand
		public MilestoneTaskCommand parent;

		@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
		private boolean help;

		@CommandLine.Parameters()
		private List<ExistingID> task;

		@Override
		public void run() {
			Project project = parent.parent.projects.getProject(parent.parent.project);

			Milestone milestone = project.getMilestone(new ExistingMilestone(project, parent.parent.milestone));

			task.forEach(milestone::addTask);

			milestone.save();
		}
	}

	@CommandLine.Command(name = "remove")
	public static class MilestoneTaskRemoveCommand implements Runnable {
		@CommandLine.ParentCommand
		public MilestoneTaskCommand parent;

		@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
		private boolean help;

		@CommandLine.Parameters()
		private List<ExistingID> task;

		@Override
		public void run() {
			Project project = parent.parent.projects.getProject(parent.parent.project);

			Milestone milestone = project.getMilestone(new ExistingMilestone(project, parent.parent.milestone));

			task.forEach(milestone::removeTask);

			milestone.save();
		}
	}
}
