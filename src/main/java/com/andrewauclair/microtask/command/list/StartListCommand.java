// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class StartListCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", completionCandidates = ListCompleter.class, description = "The list to start.")
	private ExistingListName list;

	public StartListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.getActiveContext().setNoActiveGroup();
		tasks.getActiveContext().setActiveList(list);
	}
}
