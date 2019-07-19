// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Task;
import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.os.ConsoleColors;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class SearchCommand extends Command {
	private final Tasks tasks;
	
	public SearchCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void print(PrintStream output, String command) {
		String searchText = command.substring(8, command.lastIndexOf("\""));
		
		List<Task> searchResults = tasks.getTasks().stream()
				.filter(task -> task.task.contains(searchText))
				.collect(Collectors.toList());
		
		output.println("Search Results (" + searchResults.size() + "):");
		output.println();
		
		for (Task task : searchResults) {
			output.println(task.description().replace(searchText, ConsoleColors.ANSI_BOLD + ConsoleColors.ANSI_REVERSED + searchText + ConsoleColors.ANSI_RESET));
		}
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("search"));
	}
}
