// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.GroupCompleter;
import com.andrewauclair.todo.task.TaskGroup;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class GroupSwitchCommand extends Command {
	private final List<CommandOption> options = Collections.singletonList(
			new CommandOption("group", CommandOption.NO_SHORTNAME, false)
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;

	GroupSwitchCommand(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		// TODO This doesn't show a nice error if the argument is missing
		
		String group = result.getStrArgument("group");
		
		if (group.equals("..")) {
			if (tasks.getActiveGroup().getFullPath().equals("/")) {
				return;
			}
			group = tasks.getActiveGroup().getParent();
		}
		TaskGroup group1 = tasks.switchGroup(group);

		output.println("Switched to group '" + group1.getFullPath() + "'");
		output.println();
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("chgrp", node(new GroupCompleter(tasks, false))));
	}
}
