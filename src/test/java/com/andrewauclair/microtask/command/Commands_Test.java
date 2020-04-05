// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Test extends CommandsBaseTestCase {
	@Test
	void creates_command_line_with_aliases() {
		commands.addAlias("tt", "times --today");

		CommandLine commandLine = commands.buildCommandLineWithAllCommands();

		commandLine.execute("tt");

		assertOutput(
				ConsoleColors.ANSI_BOLD + "times --today" + ConsoleColors.ANSI_RESET,
				"Times for day 12/31/1969",
				"",
				"",
				" 0s   Total",
				""
		);
	}

	@Test
	void prints_stack_for_exceptions() {
		CommandLine commandLine = commands.buildCommandLine("times");


		commandLine.addSubcommand("throw-exception", new Runnable() {
			@CommandLine.Option(names = {"-h", "--help"}, description = "Show this help message.", usageHelp = true)
			private boolean help;

			@Override
			public void run() {
				throw new RuntimeException();
			}
		});

		commandLine.execute("throw-exception");

		assertThat(errorStream).asString().startsWith("java.lang.RuntimeException" + Utils.NL +
				"\tat com.andrewauclair.microtask.command.Commands_Test$1.run(Commands_Test.java:41)");
	}
}
