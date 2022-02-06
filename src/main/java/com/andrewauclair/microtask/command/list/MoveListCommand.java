// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class MoveListCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", completionCandidates = ListCompleter.class, description = "List to move.")
	private ExistingListName list;

	@CommandLine.Option(names = {"--dest-group"}, required = true, completionCandidates = GroupCompleter.class, description = "Destination group for list or group.")
	private ExistingGroupName dest_group;

	public MoveListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.moveList(list, dest_group);

		System.out.println("Moved list " + list + " to group '" + dest_group + "'");
		System.out.println();
	}
}
