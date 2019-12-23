// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class GroupCreateCommand extends Command {
	private final List<CommandOption> options = Collections.singletonList(
			new CommandOption("group", CommandOption.NO_SHORTNAME, false)
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;

	GroupCreateCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		String group = result.getStrArgument("group").toLowerCase();

		TaskGroup group1 = tasks.createGroup(group);

		output.println("Created group '" + group1.getFullPath() + "'");
		output.println();
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("mkgrp"));
	}
}
