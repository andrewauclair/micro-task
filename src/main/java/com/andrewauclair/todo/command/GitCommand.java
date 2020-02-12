// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "git")
public class GitCommand extends Command {
	@CommandLine.Parameters(index = "0")
	private String command;

	private final OSInterface osInterface;
	
	GitCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		osInterface.runGitCommand("git " + command, true);
	}
}
