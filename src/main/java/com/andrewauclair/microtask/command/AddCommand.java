// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import static picocli.CommandLine.Option;

@Command(name = "add", synopsisSubcommandLabel = "COMMAND", description = "Add a task, list, group, project, feature or milestone.")
final class AddCommand implements Runnable {
	@Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
	private boolean help;

	@CommandLine.Spec
	private CommandLine.Model.CommandSpec spec;

	@Override
	public void run() {
		spec.commandLine().usage(System.out);
	}
}
