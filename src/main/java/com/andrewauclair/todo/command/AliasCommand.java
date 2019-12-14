// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class AliasCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("name", 'n', Collections.singletonList("Name")),
			new CommandOption("command", 'c', Collections.singletonList("Command"))
	);
	private final CommandParser parser = new CommandParser(options);
	
	private final Commands commands;
	private final OSInterface osInterface;
	
	public AliasCommand(Commands commands, OSInterface osInterface) {
		this.commands = commands;
		this.osInterface = osInterface;
	}
	
	@Override
	public void execute(PrintStream output, String command) {
		// TODO Make sure that the command doesn't already exist
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();
		
		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}
		
		output.println("Created alias '" + argsMap.get("name").getValue() + "' for '" + argsMap.get("command").getValue() + "'");
		output.println();
		
		commands.addAlias(argsMap.get("name").getValue(), argsMap.get("command").getValue());
		
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
		
		osInterface.runGitCommand("git add aliases.txt", false);
		osInterface.runGitCommand("git commit -m \"Added alias '" + argsMap.get("name").getValue() + "' for command '" + argsMap.get("command").getValue() + "'\"", false);
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.emptyList();
	}
}
