// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.ListCompleter;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.Command;

import static picocli.CommandLine.Option;

@Command(name = "add")
final class AddCommand implements Runnable {
	private final Tasks tasks;
	private final Commands commands;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-n", "--name"}, required = true)
	private String name;

	@Option(names = {"-l", "--list"}, completionCandidates = ListCompleter.class)
	private String list;

	@Option(names = {"-r", "--recurring"})
	private boolean recurring;

	@Option(names = {"-s", "--start"})
	private boolean start;

	AddCommand(Tasks tasks, Commands commands) {
		this.tasks = tasks;
		this.commands = commands;
	}

	@Override
	public void run() {
		String list = tasks.getActiveList();

		if (this.list != null) {
			list = this.list;
		}

		Task task = tasks.addTask(name, list);

		if (recurring) {
			task = tasks.setRecurring(task.id, true);
		}

		System.out.println("Added task " + task.description());

		if (this.list != null) {
			System.out.println("to list '" + tasks.getAbsoluteListName(this.list) + "'");
		}

		System.out.println();

		if (start) {
			commands.execute(System.out, "start " + task.id);
		}
	}
}
