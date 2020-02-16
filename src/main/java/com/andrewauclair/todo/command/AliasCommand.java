// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "alias")
public class AliasCommand extends Command {
	@CommandLine.Option(names = {"-n", "--name"})
	private String name;

	@CommandLine.Option(names = {"-c", "--command"})
	private String command;

	@CommandLine.Option(names = {"-u", "--update"})
	private String update = null;

	@CommandLine.Option(names = {"-r", "--remove"})
	private boolean remove;

	@CommandLine.Option(names = {"-l", "--list"})
	private boolean list;
	
	private final Commands commands;
	private final OSInterface osInterface;
	
	AliasCommand(Commands commands, OSInterface osInterface) {
		this.commands = commands;
		this.osInterface = osInterface;
	}

	private void writeAliasesFile() {
		try (DataOutputStream outputStream = osInterface.createOutputStream("git-data/aliases.txt")) {
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
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void run() {
		// TODO Command should fail if command, update and/or remove are all present
		if (command != null) {
			if (commands.getAliases().containsKey(name)) {
				System.out.println("Alias '" + name + "' already exists.");
				System.out.println();
			}
			else {
				System.out.println("Created alias '" + name + "' for command '" + command + "'");
				System.out.println();

				commands.addAlias(name, command);

				writeAliasesFile();

				osInterface.runGitCommand("git add .", false);
				osInterface.runGitCommand("git commit -m \"Added alias '" + name + "' for command '" + command + "'\"", false);
			}
		}
		else if (update != null) {
			if (commands.getAliases().containsKey(name)) {
				commands.removeAlias(name);

				System.out.println("Updated alias '" + name + "' to command '" + update + "'");
				System.out.println();

				commands.addAlias(name, update);

				writeAliasesFile();

				osInterface.runGitCommand("git add .", false);
				osInterface.runGitCommand("git commit -m \"Updated alias '" + name + "' to command '" + update + "'\"", false);
			}
			else {
				System.out.println("Alias '" + name + "' does not exist.");
				System.out.println();
			}
		}
		else if (remove) {
			String aliasCommand = commands.getAliases().get(name);

			if (aliasCommand != null) {
				System.out.println("Removed alias '" + name + "' for command '" + aliasCommand + "'");
				System.out.println();

				commands.removeAlias(name);

				writeAliasesFile();

				osInterface.runGitCommand("git add .", false);
				osInterface.runGitCommand("git commit -m \"Removed alias '" + name + "' for command '" + aliasCommand + "'\"", false);
			}
			else {
				System.out.println("Alias '" + name + "' not found.");
				System.out.println();
			}
		}
		else if (list) {
			Map<String, String> aliases = commands.getAliases();

			for (String name : aliases.keySet()) {
				System.out.println("'" + name + "' = '" + aliases.get(name) + "'");
			}
			System.out.println();
		}
		else {
			System.out.println("Invalid command.");
			System.out.println();
		}
	}
}
