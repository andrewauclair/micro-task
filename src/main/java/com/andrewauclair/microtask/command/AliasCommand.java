// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

@Command(name = "alias", description = "Add, list, update or delete aliases.")
final class AliasCommand implements Runnable {
	private final Commands commands;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-n", "--name"}, description = "The name of the alias.")
	private String name;

	@CommandLine.ArgGroup(multiplicity = "1")
	private Args args;

	private static class Args {
		@Option(names = {"-c", "--command"}, description = "The alias command.")
		private String command;

		@Option(names = {"-u", "--update"}, description = "Update alias command.")
		private String update;

		@Option(names = {"-r", "--remove"}, description = "Remove alias command.")
		private boolean remove;

		@Option(names = {"-l", "--list"}, description = "List alias commands.")
		private boolean list;
	}


	AliasCommand(Commands commands, OSInterface osInterface) {
		this.commands = commands;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (args.command != null) {
			if (name == null) {
				System.out.println("Option 'command' requires option 'name'");
				System.out.println();
				return;
			}

			if (commands.getAliases().containsKey(name)) {
				System.out.println("Alias '" + name + "' already exists.");
				System.out.println();
			}
			else {
				if (commands.buildCommandLineWithAllCommands().getSubcommands().containsKey(name)) {
					System.out.println("'" + name + "' is a command and cannot be used for an alias name.");
					System.out.println();
				}
				else if (commands.isValidCommand(args.command)) {
					System.out.println("Created alias '" + name + "' for command '" + args.command + "'");
					System.out.println();

					commands.addAlias(name, args.command);

					writeAliasesFile();

					osInterface.gitCommit("Added alias '" + name + "' for command '" + args.command + "'");
				}
				else {
					System.out.println();
					System.out.println("Command '" + args.command + "' is invalid.");
					System.out.println();
				}
			}
		}
		else if (args.update != null) {
			if (name == null) {
				System.out.println("Option 'update' requires option 'name'");
				System.out.println();
				return;
			}

			if (commands.getAliases().containsKey(name)) {
				if (commands.isValidCommand(args.update)) {
					commands.removeAlias(name);

					System.out.println("Updated alias '" + name + "' to command '" + args.update + "'");
					System.out.println();

					commands.addAlias(name, args.update);

					writeAliasesFile();

					osInterface.gitCommit("Updated alias '" + name + "' to command '" + args.update + "'");
				}
				else {
					System.out.println();
					System.out.println("Command '" + args.update + "' is invalid.");
					System.out.println();
				}
			}
			else {
				System.out.println("Alias '" + name + "' does not exist.");
				System.out.println();
			}
		}
		else if (args.remove) {
			if (name == null) {
				System.out.println("Option 'remove' requires option 'name'");
				System.out.println();
				return;
			}

			String aliasCommand = commands.getAliases().get(name);

			if (aliasCommand != null) {
				System.out.println("Removed alias '" + name + "' for command '" + aliasCommand + "'");
				System.out.println();

				commands.removeAlias(name);

				writeAliasesFile();

				osInterface.gitCommit("Removed alias '" + name + "' for command '" + aliasCommand + "'");
			}
			else {
				System.out.println("Alias '" + name + "' not found.");
				System.out.println();
			}
		}
		else if (args.list) {
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

	private void writeAliasesFile() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/aliases.txt"))) {
			Map<String, String> aliases = commands.getAliases();

			for (String name : aliases.keySet()) {
				String aliasCommand = aliases.get(name);

				outputStream.print(name);
				outputStream.print("=\"");
				outputStream.print(aliasCommand);
				outputStream.println("\"");
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
