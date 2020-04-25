// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.list;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "list")
public class RenameListCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

//	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "List to rename.")
	@Parameters(index = "0", completionCandidates = ListCompleter.class, description = "List to rename.")
	private ExistingTaskListName list;

	@Option(names = {"-n", "--name"}, required = true, description = "The new name for the list.")
	private String name;

	public RenameListCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		if (name.contains("/")) {
			throw new TaskException("Lists must be renamed with name, not paths.");
		}

		if (list.absoluteName().substring(1).contains("/")) {
			throw new TaskException("Lists must be renamed with name, not paths.");
		}

		NewTaskListName newName = new NewTaskListName(tasks, name);
		tasks.renameList(list, newName);

		System.out.println("Renamed list '" + list + "' to '" + newName + "'");
		System.out.println();
	}
}
