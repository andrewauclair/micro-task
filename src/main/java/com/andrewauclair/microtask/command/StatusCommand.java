// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.StatusConsole;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "status")
public class StatusCommand implements Runnable {
	private final OSInterface osInterface;
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"-c", "--command"}, required = true)
	private String command;

	StatusCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		osInterface.sendStatusMessage(StatusConsole.TransferType.Command, command);
	}
}
