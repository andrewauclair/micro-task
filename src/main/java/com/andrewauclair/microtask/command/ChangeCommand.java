// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "ch", description = "Change the current list or group.")
final class ChangeCommand implements Runnable {
	private final Tasks tasks;
	private final LocalSettings localSettings;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@ArgGroup(multiplicity = "1")
	private ListGroup listGroup;

	ChangeCommand(Tasks tasks, LocalSettings localSettings) {
		this.tasks = tasks;
		this.localSettings = localSettings;
	}

	@Override
	public void run() {
		if (listGroup.list != null) {
			tasks.setActiveList(listGroup.list);
			tasks.setActiveGroup(listGroup.list.parentGroupName());

			localSettings.setActiveList(listGroup.list);
			localSettings.setActiveGroup(listGroup.list.parentGroupName());

			System.out.println("Switched to list '" + listGroup.list + "'");
		}
		else {
			tasks.setActiveGroup(listGroup.group);

			localSettings.setActiveGroup(listGroup.group);

			System.out.println("Switched to group '" + listGroup.group + "'");
		}
		System.out.println();
	}

	private static final class ListGroup {
		@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "The list to change to.")
		private ExistingTaskListName list;

		@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "The group to change to.")
		private ExistingTaskGroupName group;
	}
}
