// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.project.NewProjectName;
import com.andrewauclair.microtask.project.Project;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "mk", description = "Make a new list, group or project.")
final class MakeCommand implements Runnable {
	private final Tasks tasks;
	private final Projects projects;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@ArgGroup(multiplicity = "1")
	private Args args;

	MakeCommand(Tasks tasks, Projects projects) {
		this.tasks = tasks;
		this.projects = projects;
	}

	@Override
	public void run() {
		if (args.list != null) {
			tasks.addList(args.list, true);

			System.out.println("Created list '" + args.list + "'");
			System.out.println();
		}

		if (args.group != null) {
			TaskGroup group = tasks.createGroup(this.args.group);

			System.out.println("Created group '" + group.getFullPath() + "'");
			System.out.println();
		}

		if (args.project != null) {
			Project project = projects.createProject(args.project.getName());

			System.out.println("Created project '" + args.project + "'");
			System.out.println();
		}
	}

	private static final class Args {
		@Option(names = {"-l", "--list"}, description = "Make a list.")
		private NewTaskListName list;

		@Option(names = {"-g", "--group"}, description = "Make a group.")
		private NewTaskGroupName group;

		@Option(names = {"-p", "--project"}, description = "Make a project.")
		private NewProjectName project;
	}
}
