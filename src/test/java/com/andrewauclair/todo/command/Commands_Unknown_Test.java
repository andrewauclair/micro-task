// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.Utils;
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
		CommandLine commandLine = commands.buildCommandLine();


		commandLine.addSubcommand("throw-exception", new Command() {
			@Override
			public void run() {
				throw new RuntimeException();
			}
		});

		commandLine.execute("throw-exception");

		assertThat(errorStream).asString().startsWith("java.lang.RuntimeException" + Utils.NL +
				"\tat com.andrewauclair.todo.command.Commands_Unknown_Test$1.run(Commands_Unknown_Test.java:31)");
	}
}
