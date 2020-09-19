// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.task;

import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(name = "task")
public class AddTaskCommand implements Runnable {
	private final Tasks tasks;
	private final Commands commands;

	@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Parameters(index = "0", description = "The name of the new task.")
	private String name;

	@CommandLine.Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class, description = "The list to add the new task to.")
	private ExistingListName list;

	@CommandLine.Option(names = {"-r", "--recurring"}, description = "Set the task to recurring.")
	private boolean recurring;

	@CommandLine.Option(names = {"-s", "--start"}, description = "Start the task immediately.")
	private boolean start;

	@CommandLine.Option(names = {"-t", "--tags"}, split = ",", description = "Tags to set on the task.")
	private List<String> tags;

	public AddTaskCommand(Tasks tasks, Commands commands) {
		this.tasks = tasks;
		this.commands = commands;
	}

	@Override
	public void run() {
		ExistingListName list = tasks.getCurrentList();

		if (this.list != null) {
			list = this.list;
		}

		Task task = tasks.addTask(name, list);

		if (recurring) {
			task = tasks.setRecurring(new ExistingID(tasks, task.id), true);
		}

		if (tags != null) {
			task = tasks.setTags(new ExistingID(tasks, task.id), tags);
		}

		System.out.println("Added task " + task.description());

		if (this.tags != null) {
			System.out.println("with tag(s): " + String.join(", ", tags));
		}

		if (this.list != null) {
			System.out.println("to list '" + this.list + "'");
		}

		System.out.println();

		if (start) {
			commands.execute(System.out, "start task " + task.id);
		}
	}
}
