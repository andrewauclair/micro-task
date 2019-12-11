// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class NextCommand extends Command {
	private final List<CommandOption> options = Collections.singletonList(
			new CommandOption("count", 'c', Collections.singletonList("Count"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	NextCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();
		
		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}
		
		int max = Integer.parseInt(argsMap.get("count").getValue());
		
		List<Task> tasks = this.tasks.getAllTasks().stream()
				.sorted(Comparator.comparingLong(o -> o.id))
				.filter(task -> task.state != TaskState.Finished)
				.filter(task -> !task.isRecurring())
				.limit(max)
				.collect(Collectors.toList());
		
		output.println("Next " + tasks.size() + " Tasks To Complete");
		output.println();
		
		tasks.forEach(task -> output.println(task.description()));
		
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("next",
						node("--count"
						),
						node("-c")
				)
		);
	}
}
