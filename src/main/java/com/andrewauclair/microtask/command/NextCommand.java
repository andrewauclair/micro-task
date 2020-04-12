// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "next", description = "Display the next tasks to be completed.")
final class NextCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-c", "--count"}, description = "Number of tasks to display.")
	private int count;

	NextCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		int max = count;

		List<Task> tasks = this.tasks.getAllTasks().stream()
				.sorted(Comparator.comparingLong(o -> o.id))
				.filter(task -> task.state != TaskState.Finished)
				.filter(task -> !task.isRecurring())
				.limit(max)
				.collect(Collectors.toList());

		System.out.println("Next " + tasks.size() + " Tasks To Complete");
		System.out.println();

		tasks.forEach(task -> System.out.println(task.description()));

		System.out.println();
	}
}
