// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import picocli.CommandLine;

@CommandLine.Command(name = "group")
public class StartGroupCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The group to start.")
	private ExistingGroupName group;

	public StartGroupCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.getActiveContext().setNoActiveList();
		tasks.getActiveContext().setActiveGroup(group);
	}
}
