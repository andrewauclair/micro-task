// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.jline.builtins.Completers.TreeCompleter.Node;

import java.io.PrintStream;
import java.util.List;

abstract class Command {
	public abstract void execute(PrintStream output, String command);
	
	public abstract List<Node> getAutoCompleteNodes();
}
