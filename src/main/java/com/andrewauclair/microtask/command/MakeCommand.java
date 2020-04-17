// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "mk", description = "Make a new list or group.")
final class MakeCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@ArgGroup(multiplicity = "1")
	private ListGroup listGroup;

	MakeCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (listGroup.list != null) {
			tasks.addList(listGroup.list, true);

			System.out.println("Created new list '" + listGroup.list + "'");
			System.out.println();
		}

		if (listGroup.group != null) {
			TaskGroup group = tasks.createGroup(this.listGroup.group);

			System.out.println("Created group '" + group.getFullPath() + "'");
			System.out.println();
		}
	}

	private static final class ListGroup {
		@Option(names = {"-l", "--list"}, description = "Make a list.")
		private NewTaskListName list;

		@Option(names = {"-g", "--group"}, description = "Make a group.")
		private NewTaskGroupName group;
	}
}
