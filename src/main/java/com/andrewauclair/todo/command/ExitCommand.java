// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "exit")
final class ExitCommand implements Runnable {
	private final OSInterface osInterface;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	ExitCommand(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public void run() {
		osInterface.exit();
	}
}
