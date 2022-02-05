// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static org.assertj.core.api.Assertions.assertThat;

class Commands_Test extends CommandsBaseTestCase {
	@Test
	void creates_command_line_with_aliases() {
		commands.addAlias("tt", "times --today");

		CommandLine commandLine = commands.buildCommandLine("tt");

		commandLine.execute("tt");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				ConsoleColors.ANSI_BOLD + "times --today" + ConsoleColors.ANSI_RESET,
				"Times for day 12/31/1969",
				"",
				u + "Time" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				"",
				" 0s    Total",
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
				"\tat com.andrewauclair.microtask.command.Commands_Test$1.run(Commands_Test.java");
	}

	@Test
	@Disabled("Our new picocli converts mean this doesn't actually happen. I need to find a TaskException that will always be thrown")
	void when_debug_is_enabled_task_exceptions_print_a_stack_trace() {
		Mockito.when(localSettings.isDebugEnabled()).thenReturn(true);

		commands.execute(printStream, "ch -l ranDOM");

		assertThat(errorStream).asString().startsWith("com.andrewauclair.microtask.TaskException: List '/random' does not exist." + Utils.NL +
				"\tat com.andrewauclair.microtask.task.Tasks.getGroupForList(Tasks.java");
	}
}
