// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "group")
public class RenameGroupCommand implements Runnable {
	private final Tasks tasks;
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(index = "0", completionCandidates = GroupCompleter.class, description = "Group to rename.")
	private ExistingTaskGroupName group;

	@Option(names = {"-n", "--name"}, required = true, description = "The new name for the group.")
	private NewTaskGroupName name;

	public RenameGroupCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		String oldGroupPath = tasks.getGroup(group.absoluteName()).getFullPath();

		tasks.renameGroup(group, name);

		System.out.println("Renamed group '" + oldGroupPath + "' to '" + name + "'");
		System.out.println();
	}
}
