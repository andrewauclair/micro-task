// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.jline.ActiveListCompleter;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class AddCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("name", 'n', Collections.singletonList("Name")),
			new CommandOption("recurring", 'r', true),
			new CommandOption("charge", 'c', Collections.singletonList("Charge")),
			new CommandOption("list", 'l', Collections.singletonList("List"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	AddCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);

		String taskTitle;
		
		if (result.hasArgument("name")) {
			taskTitle = result.getStrArgument("name");
		}
		else {
			throw new TaskException("Missing name argument.");
		}

		String list = tasks.getActiveList();
		
		if (result.hasArgument("list")) {
			list = result.getStrArgument("list");
		}

		Task task = tasks.addTask(taskTitle, list);
		
		if (result.hasArgument("recurring")) {
			task = tasks.setRecurring(task.id, true);
		}
		
		output.println("Added task " + task.description());
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("add",
						node("--list",
								node(new ActiveListCompleter(tasks))
						),
						node("-l",
								node(new ActiveListCompleter(tasks))
						),
						node("--name"),
						node("-n")
				)//,
//				node("add",
//				)
		);
	}
}
