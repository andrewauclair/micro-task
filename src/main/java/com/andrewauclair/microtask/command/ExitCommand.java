// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.StatusConsole.TransferType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "exit", description = "Exit the application.")
final class ExitCommand implements Runnable {
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	ExitCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		osInterface.sendStatusMessage(TransferType.EXIT);
		osInterface.exit();
	}
}
