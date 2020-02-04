// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
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
	private final List<String> usage = Arrays.asList(
			"add --name \"Task Name\"",
			"add -s -n \"Task Name\"",
			"add -l /default -n \"Task Name\"",
			"add --list /default --recurring true -n \"Task Name\""
	);
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("help", 'h', "Show this screen", Collections.emptyList()),
			new CommandOption("name", 'n', "Name of the task", Collections.singletonList("name")),
			new CommandOption("recurring", 'r', "Whether or not the task is recurring", true),
			new CommandOption("list", 'l', "List to add the task to", Collections.singletonList("list")),
			new CommandOption("start", 's', "Start the task immediately", Collections.emptyList())
	);
	private final CommandParser parser = new CommandParser("Add", usage, options);
	private final Tasks tasks;
	private final Commands commands;

	AddCommand(Tasks tasks, Commands commands) {
		this.tasks = tasks;
		this.commands = commands;
	}

	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);

		if (result.hasArgument("help")) {
			parser.printHelp(output);
			return;
		}

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

		if (result.hasArgument("start")) {
			commands.execute(output, "start " + task.id);
		}
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
