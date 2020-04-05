// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "debug")
public final class DebugCommand implements Runnable {
	private final LocalSettings localSettings;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--enable"})
	private boolean enable;

	@Option(names = {"--disable"})
	private boolean disable;

	public DebugCommand(LocalSettings localSettings) {
		this.localSettings = localSettings;
	}

	@Override
	public void run() {
		localSettings.setDebugEnabled(enable);
	}
}
