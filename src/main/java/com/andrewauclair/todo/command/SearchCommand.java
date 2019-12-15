// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class SearchCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("text", 't', Collections.singletonList("Text")),
			new CommandOption("finished", 'f'),
			new CommandOption("group", 'g')
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	SearchCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		String searchText = result.getStrArgument("text");
		
		Stream<Task> stream;
		
		if (result.hasArgument("group")) {
			stream = tasks.getActiveGroup().getTasks().stream();
		}
		else {
			stream = tasks.getTasks().stream();
		}
		
		List<Task> searchResults = stream.filter(task -> task.task.contains(searchText))
				.filter(task -> result.hasArgument("finished") == (task.state == TaskState.Finished))
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
