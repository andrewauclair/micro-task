// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class ListCreateCommand extends Command {
	private final Tasks tasks;
	
	public ListCreateCommand(Tasks tasks) {
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
		
		boolean added = tasks.addList(list);
		
		if (added) {
			output.println("Created new list '" + list + "'");
		}
		else {
			output.println("List '" + list + "' already exists.");
		}
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("create-list"));
	}
}
