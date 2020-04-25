// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "task")
public class RenameTaskCommand implements Runnable {
	private final Tasks tasks;
	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Parameters(description = "Task to rename.")
	private ExistingID id;

	@CommandLine.Option(names = {"-n", "--name"}, required = true, description = "The new name for the task.")
	private String name;

	public RenameTaskCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		Task task = tasks.renameTask(id, name);

		System.out.println("Renamed task " + task.description());
		System.out.println();
	}
}
