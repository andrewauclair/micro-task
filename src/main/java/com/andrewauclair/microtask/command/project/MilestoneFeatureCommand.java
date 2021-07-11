// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.project;

import com.andrewauclair.microtask.command.MilestoneCommand;
import com.andrewauclair.microtask.project.ExistingFeature;
import com.andrewauclair.microtask.project.ExistingMilestone;
import com.andrewauclair.microtask.project.Milestone;
import com.andrewauclair.microtask.project.Project;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "feature", subcommands = {
		MilestoneFeatureCommand.MilestoneFeatureAddCommand.class,
		MilestoneFeatureCommand.MilestoneFeatureRemoveCommand.class
})
public class MilestoneFeatureCommand implements Runnable {
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
	public static class MilestoneFeatureAddCommand implements Runnable {
		@CommandLine.ParentCommand
		public MilestoneFeatureCommand parent;

		@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
		private boolean help;

		@CommandLine.Parameters()
		private List<String> feature;

		@Override
		public void run() {
			Project project = parent.parent.projects.getProject(parent.parent.project);

			Milestone milestone = project.getMilestone(new ExistingMilestone(project, parent.parent.milestone));

			feature.forEach(feat -> milestone.addFeature(ExistingFeature.tryCreate(project, feat)));

			milestone.save();
		}
	}

	@CommandLine.Command(name = "remove")
	public static class MilestoneFeatureRemoveCommand implements Runnable {
		@CommandLine.ParentCommand
		public MilestoneFeatureCommand parent;

		@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
		private boolean help;

		@CommandLine.Parameters()
		private List<String> feature;

		@Override
		public void run() {
			Project project = parent.parent.projects.getProject(parent.parent.project);

			Milestone milestone = project.getMilestone(new ExistingMilestone(project, parent.parent.milestone));

			feature.forEach(feat -> milestone.removeFeature(ExistingFeature.tryCreate(project, feat)));

			milestone.save();
		}
	}
}
