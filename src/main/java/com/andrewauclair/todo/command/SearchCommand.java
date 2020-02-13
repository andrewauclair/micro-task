// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "search")
public class SearchCommand extends Command {
	@CommandLine.Option(names = {"-t", "--text"})
	private String text;

	@CommandLine.Option(names = {"-f", "--finished"})
	private boolean finished;

	@CommandLine.Option(names = {"-g", "--group"}, completionCandidates = GroupCompleter.class)
	private boolean group;

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
				.collect(Collectors.toList());

		System.out.println("Search Results (" + searchResults.size() + "):");
		System.out.println();

		for (Task task : searchResults) {
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
//			"a;b;c;d".split())
//			output.println(task.description().replaceAll("(?i)" + searchText, ConsoleColors.ANSI_BOLD + ConsoleColors.ANSI_REVERSED + searchText + ConsoleColors.ANSI_RESET));
		}
		System.out.println();
	}
}
