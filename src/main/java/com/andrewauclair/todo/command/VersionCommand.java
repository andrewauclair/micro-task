// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Tasks;
import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class VersionCommand extends Command {
	private final Tasks tasks;
	private final OSInterface osInterface;

	public VersionCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		try {
			output.println(osInterface.getVersion());
		}
		catch (IOException e) {
			output.println("Unknown");
		}
		output.println();
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("version"));
	}
}
