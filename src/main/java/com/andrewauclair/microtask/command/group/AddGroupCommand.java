// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.group;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import picocli.CommandLine;

@CommandLine.Command(name = "group")
public class AddGroupCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", completionCandidates = GroupCompleter.class, description = "Group to add.")
	private NewTaskGroupName group;

	@CommandLine.Option(names = {"--time-category"}, description = "Time category used for time charging.")
	private String time_category;

	public AddGroupCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		TaskGroup group = tasks.createGroup(this.group, time_category != null ? time_category : "");

		System.out.print("Created group '" + group.getFullPath() + "'");

		if (time_category != null) {
			System.out.print(" with Time Category '" + time_category + "'");
		}
		System.out.println();
		System.out.println();
	}
}
