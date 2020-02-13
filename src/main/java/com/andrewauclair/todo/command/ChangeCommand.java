// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

@CommandLine.Command(name = "ch")
public class ChangeCommand extends Command {
	private final Tasks tasks;
	@CommandLine.ArgGroup(multiplicity = "1")
	private ListGroup listGroup;

	public ChangeCommand(Tasks tasks) {

		this.tasks = tasks;
	}

	static class ListGroup {
		@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
		private String list;

		@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
		private String group;
	}

	@Override
	public void run() {
		if (listGroup.list != null) {
			String list = this.listGroup.list.toLowerCase();

			tasks.setActiveList(list);

			String actualList = tasks.getAbsoluteListName(list);

			String group = tasks.getGroupForList(actualList).getFullPath();

			tasks.switchGroup(group);

			System.out.println("Switched to list '" + actualList + "'");
			System.out.println();
		}
		else if (listGroup.group != null) {
			String group = this.listGroup.group;

			if (group.equals("..")) {
				if (tasks.getActiveGroup().getFullPath().equals("/")) {
					return;
				}
				group = tasks.getActiveGroup().getParent();
			}
			TaskGroup group1 = tasks.switchGroup(group);

			System.out.println("Switched to group '" + group1.getFullPath() + "'");
			System.out.println();
		}
	}
}
