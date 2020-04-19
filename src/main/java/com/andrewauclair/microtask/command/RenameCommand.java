// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "rename", description = "Rename a task, list or group.")
final class RenameCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.ArgGroup(multiplicity = "1")
	private Args args;

	private static class Args {
		@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List to rename.")
		private ExistingTaskListName list;

		@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "Group to rename.")
		private ExistingTaskGroupName group;

		@Option(names = {"-t", "--task"}, description = "Task to rename.")
		private ExistingID id;
	}

	// TODO This needs to be a NewTaskListName (list), NewTaskGroupName (group) or String (task)
	@Option(names = {"-n", "--name"}, required = true, description = "The new name for the task, list or group.")
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

			if (args.list.absoluteName().substring(1).contains("/")) {
				throw new TaskException("Lists must be renamed with name, not paths.");
			}

			NewTaskListName newName = new NewTaskListName(tasks, name);
			tasks.renameList(args.list, newName);

			System.out.println("Renamed list '" + args.list + "' to '" + newName + "'");
			System.out.println();
		}
		else if (args.group != null) {
			if (!args.group.absoluteName().endsWith("/")) {
				System.out.println("Old group name should end with /");
				System.out.println();
			}
			else if (!name.endsWith("/")) {
				System.out.println("New group name should end with /");
				System.out.println();
			}
			else {
				String oldGroupPath = tasks.getGroup(args.group.absoluteName()).getFullPath();

				tasks.renameGroup(args.group, new NewTaskGroupName(tasks, name));

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
