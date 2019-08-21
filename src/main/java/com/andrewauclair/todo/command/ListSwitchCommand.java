// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ListCompleter;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class ListSwitchCommand extends Command {
	private final Tasks tasks;
	
	public ListSwitchCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		if (s.length != 2) {
			output.println("Invalid command.");
			output.println();
			return;
		}

		String listParameter = s[1].toLowerCase();

		boolean exists = tasks.setCurrentList(listParameter);
		
		String list = tasks.getAbsoluteListName(listParameter);
		
		String group = tasks.groupNameFromList(list);
		
		tasks.switchGroup(group);

		if (exists) {
			output.println("Switched to list '" + list + "'");
		}
		else {
			output.println("List '" + list + "' does not exist.");
		}
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(
				node("chlist",
						node(new ListCompleter(tasks, false))
				)
		);
	}
}
