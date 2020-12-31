// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Unknown_Test extends CommandsBaseTestCase {
	@Test
	void prints_unknown_command_when_command_is_not_found() {
		commands.addAlias("tt", "times --today");
		
		commands.execute(printStream, "junk");

		assertOutput(
				"Unmatched argument at index 0: 'junk'",
				""
		);
	}

	@Test
	void prints_stack_for_exceptions() {
		CommandLine commandLine = commands.buildCommandLineWithAllCommands();


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
				"\tat com.andrewauclair.microtask.command.Commands_Unknown_Test$1.run(Commands_Unknown_Test.java:34)");
	}
}
