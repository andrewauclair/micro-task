// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class ListCreateCommand extends Command {
	private final List<CommandOption> options = Collections.singletonList(
			new CommandOption("list", CommandOption.NO_SHORTNAME, false)
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	
	ListCreateCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		if (!result.hasArgument("list")) {
			output.println("Missing 'list' argument.");
			output.println();
			return;
		}
		
		String list = result.getStrArgument("list").toLowerCase();
		
		boolean added = tasks.addList(list);

		String actualList = tasks.getAbsoluteListName(list);

		if (added) {
			output.println("Created new list '" + actualList + "'");
		}
		else {
			output.println("List '" + actualList + "' already exists.");
		}
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("mklist"));
	}
}
