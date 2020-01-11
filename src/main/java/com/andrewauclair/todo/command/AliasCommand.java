// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AliasCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("name", 'n', Collections.singletonList("Name")),
			new CommandOption("command", 'c', Collections.singletonList("Command")),
			new CommandOption("remove", 'r', true),
			new CommandOption("list", 'l', true)
	);
	private final CommandParser parser = new CommandParser(options);
	
	private final Commands commands;
	private final OSInterface osInterface;
	
	AliasCommand(Commands commands, OSInterface osInterface) {
		this.commands = commands;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		// TODO Make sure that the command doesn't already exist
		// TODO Can aliases be updated by setting them again or do they need to be removed first?
		CommandParser.CommandParseResult result = parser.parse(command);
		
		// TODO Command should fail if command and remove are both present
		String nameArg = result.hasArgument("name") ? result.getStrArgument("name") : "";
		
		if (result.hasArgument("command")) {
			output.println("Created alias '" + nameArg + "' for command '" + result.getStrArgument("command") + "'");
			output.println();
			
			commands.addAlias(nameArg, result.getStrArgument("command"));
			
			writeAliasesFile();
			
			osInterface.runGitCommand("git add aliases.txt", false);
			osInterface.runGitCommand("git commit -m \"Added alias '" + nameArg + "' for command '" + result.getStrArgument("command") + "'\"", false);
		}
		else if (result.hasArgument("remove")) {
			String aliasCommand = commands.getAliases().get(nameArg);
			
			if (aliasCommand != null) {
				output.println("Removed alias '" + nameArg + "' for command '" + aliasCommand + "'");
				output.println();
				
				commands.removeAlias(nameArg);
				
				writeAliasesFile();
				
				osInterface.runGitCommand("git add aliases.txt", false);
				osInterface.runGitCommand("git commit -m \"Removed alias '" + nameArg + "' for command '" + aliasCommand + "'\"", false);
			}
			else {
				output.println("Alias '" + nameArg + "' not found.");
				output.println();
			}
		}
		else if (result.hasArgument("list")) {
			Map<String, String> aliases = commands.getAliases();
			
			for (String name : aliases.keySet()) {
				output.println("'" + name + "' = '" + aliases.get(name) + "'");
			}
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	private void writeAliasesFile() {
		try {
			DataOutputStream outputStream = osInterface.createOutputStream("git-data/aliases.txt");
			
			Map<String, String> aliases = commands.getAliases();
			
			for (String name : aliases.keySet()) {
				String aliasCommand = aliases.get(name);
				
				outputStream.write(name.getBytes());
				outputStream.write("=\"".getBytes());
				outputStream.write(aliasCommand.getBytes());
				outputStream.write("\"".getBytes());
				outputStream.write(Utils.NL.getBytes());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			
			// TODO Test exception
		}
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.emptyList();
	}
}
