// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "debug")
public final class DebugCommand implements Runnable {
	private static boolean debugEnabled = false;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--enable"})
	private boolean enable;

	@Option(names = {"--disable"})
	private boolean disable;

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@Override
	public void run() {
		debugEnabled = enable;
	}
}
