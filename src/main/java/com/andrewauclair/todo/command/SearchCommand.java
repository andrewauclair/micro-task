// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
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
			new CommandOption("finished", 'f', true),
			new CommandOption("group", 'g', true)
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
		
		List<Task> searchResults = stream.filter(task -> task.task.toLowerCase().contains(searchText.toLowerCase()))
				.filter(task -> result.hasArgument("finished") == (task.state == TaskState.Finished))
				.collect(Collectors.toList());
		
		output.println("Search Results (" + searchResults.size() + "):");
		output.println();
		
		for (Task task : searchResults) {
			boolean highlight = false;
			for (String str : task.description().split("((?<=(?i)" + searchText + ")|(?=(?i)" + searchText + "))")) {
				if (highlight) {
					output.print(ConsoleColors.ANSI_BOLD + ConsoleColors.ANSI_REVERSED);
				}
				output.print(str);
				if (highlight) {
					output.print(ConsoleColors.ANSI_RESET);
				}
				highlight = !highlight;
			}
			output.println();
//			"a;b;c;d".split())
//			output.println(task.description().replaceAll("(?i)" + searchText, ConsoleColors.ANSI_BOLD + ConsoleColors.ANSI_REVERSED + searchText + ConsoleColors.ANSI_RESET));
		}
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("search"));
	}
}
