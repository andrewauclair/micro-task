// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "rename")
final class RenameCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.ArgGroup(multiplicity = "1")
	private Args args;

	private static class Args {
		@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;

		@Option(names = {"-t", "--task"})
		private Long id;
	}

	@Option(names = {"-n", "--name"}, required = true)
	private String name;

	RenameCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (args.list != null) {
			if (name.contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}

			if (args.list.contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}

			tasks.renameList(args.list, name);

			System.out.println("Renamed list '" + tasks.getAbsoluteListName(args.list) + "' to '" + tasks.getAbsoluteListName(name) + "'");
			System.out.println();
		}
		else if (args.group != null) {
			if (!args.group.endsWith("/")) {
				System.out.println("Old group name should end with /");
				System.out.println();
			}
			else if (!name.endsWith("/")) {
				System.out.println("New group name should end with /");
				System.out.println();
			}
			else {
				String oldGroupPath = tasks.getGroup(args.group).getFullPath();

				tasks.renameGroup(args.group, name);

				System.out.println("Renamed group '" + oldGroupPath + "' to '" + tasks.getGroup(name).getFullPath() + "'");
				System.out.println();
			}
		}
		else {
			Task task = tasks.renameTask(args.id, name);

			System.out.println("Renamed task " + task.description());
			System.out.println();
		}
	}
}
