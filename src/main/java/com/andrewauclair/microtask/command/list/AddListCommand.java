// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class AddListCommand implements Runnable {
	private final Tasks tasks;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", completionCandidates = ListCompleter.class, description = "List to add.")
	private NewTaskListName list;

	public AddListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.addList(list, true);

		System.out.println("Created list '" + list + "'");
		System.out.println();
	}
}
