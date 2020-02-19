// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskList;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.Tasks;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "search")
public class SearchCommand extends Command {
	@CommandLine.Option(names = {"-t", "--text"})
	private String text;

	@CommandLine.Option(names = {"-f", "--finished"})
	private boolean finished;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
	private boolean group;

	@CommandLine.Option(names = {"-v", "--verbose"})
	private boolean verbose;

	private final Tasks tasks;

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
