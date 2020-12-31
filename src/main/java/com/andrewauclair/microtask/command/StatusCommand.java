// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.StatusConsole;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "status", description = "Send a command to the status console.")
public class StatusCommand implements Runnable {
	private final Commands commands;
	private final OSInterface osInterface;
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-c", "--command"}, required = true, description = "Command to send to the status console.")
	private String command;

	StatusCommand(Commands commands, OSInterface osInterface) {
		this.commands = commands;
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		if (commands.isValidCommand(command)) {
			osInterface.sendStatusMessage(StatusConsole.TransferType.COMMAND, command);
		}
		else {
			System.out.println();
			System.out.println("Command '" + command + "' is invalid.");
			System.out.println();
		}
	}
}
