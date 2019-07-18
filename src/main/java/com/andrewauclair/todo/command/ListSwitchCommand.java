// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.jline.ListCompleter;
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
	public void print(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		if (s.length != 2) {
			output.println("Invalid command.");
			output.println();
			return;
		}
		
		String list = s[1].toLowerCase();
		
		boolean exists = tasks.setCurrentList(list);
		
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
				node("switch-list",
						node(new ListCompleter(tasks))
				)
		);
	}
}
