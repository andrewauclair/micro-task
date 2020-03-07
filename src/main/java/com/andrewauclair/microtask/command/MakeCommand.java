// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "mk")
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
			String list = this.listGroup.list.toLowerCase();

			boolean added = tasks.addList(list, true);

			String actualList = tasks.getAbsoluteListName(list);

			if (added) {
				System.out.println("Created new list '" + actualList + "'");
			}
			else {
				System.out.println("List '" + actualList + "' already exists.");
			}
			System.out.println();
		}

		if (listGroup.group != null) {
			TaskGroup group = tasks.createGroup(this.listGroup.group.toLowerCase());

			System.out.println("Created group '" + group.getFullPath() + "'");
			System.out.println();
		}
	}

	private static final class ListGroup {
		@Option(names = {"-l", "--list"})
		private String list;

		@Option(names = {"-g", "--group"})
		private String group;
	}
}
