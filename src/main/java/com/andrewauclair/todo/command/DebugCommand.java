// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "debug")
public class DebugCommand extends Command {
	@CommandLine.Option(names = {"--enable"})
	private boolean enable;

	@CommandLine.Option(names = {"--disable"})
	private boolean disable;

	private static boolean debugEnabled = false;
	
	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@Override
	public void run() {
		debugEnabled = enable;
	}
}
