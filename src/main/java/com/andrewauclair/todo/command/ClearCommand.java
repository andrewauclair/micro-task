// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class ClearCommand extends Command {
	private final OSInterface osInterface;
	
	public ClearCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		osInterface.clearScreen();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("clear"));
	}
}
