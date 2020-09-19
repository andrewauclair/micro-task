// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.project.ExistingProject;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "ch", description = "Change the current list or group.")
final class ChangeCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;
	private final LocalSettings localSettings;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@ArgGroup(multiplicity = "1")
	private Args args;

	ChangeCommand(Tasks tasks, Projects projects, LocalSettings localSettings) {
		this.tasks = tasks;
		this.projects = projects;
		this.localSettings = localSettings;
	}

	@Override
	public void run() {
		if (args.list != null) {
			tasks.setCurrentList(args.list);
			tasks.setCurrentGroup(args.list.parentGroupName());

			localSettings.setActiveList(args.list);
			localSettings.setActiveGroup(args.list.parentGroupName());

			System.out.println("Switched to list '" + args.list + "'");
		}
		else if (args.group != null) {
			tasks.setCurrentGroup(args.group);

			localSettings.setActiveGroup(args.group);

			System.out.println("Switched to group '" + args.group + "'");
		}
		else {
			projects.setActiveProject(args.project);

			System.out.println("Switched to project '" + args.project + "'");
		}
		System.out.println();
	}

	private static final class Args {
		@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "The list to change to.")
		private ExistingListName list;

		@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "The group to change to.")
		private ExistingGroupName group;

		@Option(names = {"-p", "--project"}, description = "The project to change to.")
		private ExistingProject project;
	}
}
