// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.StatusConsole.TransferType;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(name = "focus", description = "Set the status bar to be in focus.")
final class FocusCommand implements Runnable {
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	FocusCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		osInterface.sendStatusMessage(TransferType.FOCUS);
	}
}
