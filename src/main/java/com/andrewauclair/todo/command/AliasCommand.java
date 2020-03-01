// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

@Command(name = "alias")
final class AliasCommand implements Runnable {
	private final Commands commands;
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-n", "--name"})
	private String name;

	@CommandLine.ArgGroup(multiplicity = "1")
	private Args args;

	private static class Args {
		@Option(names = {"-c", "--command"})
		private String command;

		@Option(names = {"-u", "--update"})
		private String update;

		@Option(names = {"-r", "--remove"})
		private boolean remove;

		@Option(names = {"-l", "--list"})
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
				System.out.println("Created alias '" + name + "' for command '" + args.command + "'");
				System.out.println();

				commands.addAlias(name, args.command);

				writeAliasesFile();

				osInterface.runGitCommand("git add .", false);
				osInterface.runGitCommand("git commit -m \"Added alias '" + name + "' for command '" + args.command + "'\"", false);
			}
		}
		else if (args.update != null) {
			if (name == null) {
				System.out.println("Option 'update' requires option 'name'");
				System.out.println();
				return;
			}

			if (commands.getAliases().containsKey(name)) {
				commands.removeAlias(name);

				System.out.println("Updated alias '" + name + "' to command '" + args.update + "'");
				System.out.println();

				commands.addAlias(name, args.update);

				writeAliasesFile();

				osInterface.runGitCommand("git add .", false);
				osInterface.runGitCommand("git commit -m \"Updated alias '" + name + "' to command '" + args.update + "'\"", false);
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

				osInterface.runGitCommand("git add .", false);
				osInterface.runGitCommand("git commit -m \"Removed alias '" + name + "' for command '" + aliasCommand + "'\"", false);
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
}
