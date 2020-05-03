// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.task.TaskContainerState;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@CommandLine.Command(name = "group")
public class SetGroupCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(completionCandidates = GroupCompleter.class, description = "The group to set.")
	private ExistingGroupName group;

	private static class Args {
		@ArgGroup(exclusive = false)
		private ProjectFeature projectFeature;

		@Option(names = {"--in-progress"}, description = "Set the list state to in progress.")
		private boolean in_progress;
	}

	static final class ProjectFeature {
		@Option(names = {"-p", "--project"}, description = "The project to set.")
		String project;

		@Option(names = {"-f", "--feature"}, description = "The feature to set.")
		String feature;
	}

	@ArgGroup(exclusive = false, multiplicity = "1")
	Args args;

	public SetGroupCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (args.projectFeature != null) {
			handleProjectAndFeature();
		}

		if (args.in_progress) {
			TaskGroup group = tasks.getGroup(this.group.absoluteName());

			if (group.getState() == TaskContainerState.Finished) {
				tasks.setGroupState(this.group, TaskContainerState.InProgress, true);

				System.out.println("Set state of group '" + group.getFullPath() + "' to In Progress");
			}
			else {
				System.out.println("Group '" + group.getFullPath() + "' must be finished first");
			}
		}

		System.out.println();
	}

	private void handleProjectAndFeature() {
		if (args.projectFeature.project != null) {
			String project = this.args.projectFeature.project;
			tasks.setProject(group, project, true);

			System.out.println("Set project for group '" + group + "' to '" + project + "'");
		}

		if (args.projectFeature.feature != null) {
			String feature = this.args.projectFeature.feature;
			tasks.setFeature(group, feature, true);

			System.out.println("Set feature for group '" + group + "' to '" + feature + "'");
		}
	}
}
