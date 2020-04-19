// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.LocalSettings;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "debug", description = "Configure the debug flag.")
public final class DebugCommand implements Runnable {
	private final LocalSettings localSettings;

	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@Option(names = {"--enable"}, description = "Enable the debug flag.")
	private boolean enable;

	@Option(names = {"--disable"}, description = "Disable the debug flag.")
	private boolean disable;

	public DebugCommand(LocalSettings localSettings) {
		this.localSettings = localSettings;
	}

	@Override
	public void run() {
		localSettings.setDebugEnabled(enable);
	}
}
