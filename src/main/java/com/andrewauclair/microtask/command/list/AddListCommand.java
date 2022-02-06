// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
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

	@CommandLine.Option(names = {"--time-category"}, description = "Time category used for time charging.")
	private String time_category;

	public AddListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		tasks.addList(list, true);

		System.out.print("Created list '" + list + "'");

		if (time_category != null) {
			System.out.print(" with Time Category '" + time_category + "'");
		}
		System.out.println();

		System.out.println();
	}
}
