// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import picocli.CommandLine;

@CommandLine.Command(name = "group")
public class MoveGroupCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", completionCandidates = GroupCompleter.class, description = "Group to move.")
	private ExistingGroupName group;

	@CommandLine.Option(names = {"--dest-group"}, required = true, completionCandidates = GroupCompleter.class, description = "Destination group for list or group.")
	private ExistingGroupName dest_group;

	public MoveGroupCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.moveGroup(group, dest_group);

		System.out.println("Moved group '" + group + "' to group '" + dest_group + "'");
		System.out.println();
	}
}
