// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.jline.GroupCompleter;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskList;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "search", description = "Search for tasks.")
final class SearchCommand implements Runnable {
	private final Tasks tasks;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-t", "--text"}, description = "The text to search for in the task names.")
	private String text;

	@Option(names = {"-f", "--finished"}, description = "Search for finished tasks.")
	private boolean finished;

	@Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class, description = "Search for tasks in the current group recursively.")
	private boolean group;

	@Option(names = {"-v", "--verbose"}, description = "Display list names in results.")
	private boolean verbose;

	SearchCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void run() {
		String searchText = text;

		Stream<Task> stream;

		if (group) {
			stream = tasks.getActiveGroup().getTasks().stream();
		}
		else {
			stream = tasks.getTasks().stream();
		}

		List<Task> searchResults = stream.filter(task -> task.task.toLowerCase().contains(searchText.toLowerCase()))
				.filter(task -> finished == (task.state == TaskState.Finished))
				.sorted(Comparator.comparingLong(o -> o.id))
				.collect(Collectors.toList());

		if (verbose) {
			searchResults.sort(Comparator.comparing(o -> tasks.findListForTask(o.id).getFullPath()));
		}

		System.out.println("Search Results (" + searchResults.size() + "):");
		System.out.println();

		String currentList = "";

		for (Task task : searchResults) {
			TaskList listForTask = tasks.findListForTask(task.id);

			if (verbose && !listForTask.getFullPath().equals(currentList)) {
				if (!currentList.isEmpty()) {
					System.out.println();
				}

				currentList = listForTask.getFullPath();

				System.out.println(ConsoleColors.ANSI_BOLD + currentList + ConsoleColors.ANSI_RESET);
			}

			boolean highlight = false;
			for (String str : task.description().split("((?<=(?i)" + searchText + ")|(?=(?i)" + searchText + "))")) {
				if (highlight) {
					System.out.print(ConsoleColors.ANSI_BOLD + ConsoleColors.ANSI_REVERSED);
				}
				System.out.print(str);
				if (highlight) {
					System.out.print(ConsoleColors.ANSI_RESET);
				}
				highlight = !highlight;
			}
			System.out.println();
		}

		System.out.println();
	}
}
