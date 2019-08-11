// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.*;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class AddCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("name", 'n', Collections.singletonList("Name")),
			new CommandOption("issue", 'i', Collections.singletonList("Issue")),
			new CommandOption("charge", 'c', Collections.singletonList("Charge")),
			new CommandOption("list", 'l', Collections.singletonList("List"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	public AddCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();
		
		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}
		
		String taskTitle;
		
		if (argsMap.containsKey("name")) {
			taskTitle = argsMap.get("name").getValue();
		}
		else {
			throw new RuntimeException("Missing name argument.");
		}

		String list = tasks.getCurrentList();

		if (argsMap.containsKey("list")) {
			list = argsMap.get("list").getValue();
		}

		Task task = tasks.addTask(taskTitle, list);
		
		if (argsMap.containsKey("issue")) {
			task = tasks.setIssue(task.id, Long.parseLong(argsMap.get("issue").getValue()));
		}
		
		if (argsMap.containsKey("charge")) {
			task = tasks.setCharge(task.id, argsMap.get("charge").getValue());
		}
		
		output.println("Added task " + task.description());
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("add"));
	}
}
