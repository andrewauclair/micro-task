// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.TaskContainerState;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "list")
public class SetListCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(completionCandidates = ListCompleter.class, description = "The list to set.")
	private ExistingListName list;

	@ArgGroup(exclusive = false, multiplicity = "1")
	Args args;

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

	public SetListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (args.projectFeature != null) {
			handleProjectAndFeature();
		}

		if (args.in_progress) {
			TaskList list = tasks.getListByName(this.list);

			if (list.getState() == TaskContainerState.Finished) {
				tasks.setListState(this.list, TaskContainerState.InProgress, true);

				System.out.println("Set state of list '" + list.getFullPath() + "' to In Progress");
			}
			else {
				System.out.println("List '" + list.getFullPath() + "' must be finished first");
			}
		}

		System.out.println();
	}

	private void handleProjectAndFeature() {
		if (args.projectFeature.project != null) {
			String project = this.args.projectFeature.project;
			tasks.setProject(list, project, true);

			System.out.println("Set project for list '" + list + "' to '" + project + "'");
		}

		if (args.projectFeature.feature != null) {
			String feature = this.args.projectFeature.feature;
			tasks.setFeature(list, feature, true);

			System.out.println("Set feature for list '" + list + "' to '" + feature + "'");
		}
	}
}
