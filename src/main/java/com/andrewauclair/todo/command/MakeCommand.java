// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "mk")
public class MakeCommand extends Command {
	private final Tasks tasks;
	@CommandLine.ArgGroup(multiplicity = "1")
	private ListGroup listGroup;

	public MakeCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	static class ListGroup {
		@CommandLine.Option(names = {"-l", "--list"})
		private String list;

		@CommandLine.Option(names = {"-g", "--group"})
		private String group;
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
}
