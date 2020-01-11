// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class DebugCommand extends Command {
	private final List<CommandOption> options = Collections.singletonList(
			new CommandOption("debug flag", CommandOption.NO_SHORTNAME, false)
	);
	private final CommandParser parser = new CommandParser(options);
	private boolean debugEnabled = false;
	
	public boolean isDebugEnabled() {
		return debugEnabled;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		CommandParser.CommandParseResult result = parser.parse(command);
		
		if (result.getStrArgument("debug flag").equals("enable")) {
			debugEnabled = true;
		}
		else if (result.getStrArgument("debug flag").equals("disable")) {
			debugEnabled = false;
		}
		else {
			output.println("Missing argument 'debug flag'.");
			output.println();
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.singletonList(node("debug"));
	}
}
