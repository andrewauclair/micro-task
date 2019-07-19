// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class DebugCommand extends Command {
	private final Tasks tasks;
	
	private boolean debugEnabled = false;
	
	public DebugCommand(Tasks tasks) {
		this.tasks = tasks;
	}
	
	public boolean isDebugEnabled() {
		return debugEnabled;
	}
	
	@Override
	public void print(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		if (s[1].equals("enable")) {
			debugEnabled = true;
		}
		else if (s[1].equals("disable")) {
			debugEnabled = false;
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("debug"));
	}
}
