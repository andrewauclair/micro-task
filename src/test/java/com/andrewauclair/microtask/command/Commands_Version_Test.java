// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;

class Commands_Version_Test extends CommandsBaseTestCase {
	@Test
	void execute_version_command() throws IOException {
		Mockito.when(osInterface.getVersion()).thenReturn("20.1.1");

		commands.execute(printStream, "version");

		ConsoleColors.ConsoleForegroundColor micro = ConsoleColors.ConsoleForegroundColor.ANSI_FG_PURPLE;
		ConsoleColors.ConsoleForegroundColor task = ConsoleColors.ConsoleForegroundColor.ANSI_FG_CYAN;

		assertOutput(
				micro + "               _                    " + task + "   __                __  " + ANSI_RESET,
				micro + "   ____ ___   (_)_____ _____ ____   " + task + "  / /_ ____ _ _____ / /__" + ANSI_RESET,
				micro + "  / __ `__ \\ / // ___// ___// __ \\  " + task + " / __// __ `// ___// //_/" + ANSI_RESET,
				micro + " / / / / / // // /__ / /   / /_/ /  " + task + "/ /_ / /_/ /(__  )/ ,<   " + ANSI_RESET + ANSI_BOLD + "20.1.1" + ANSI_RESET,
				micro + "/_/ /_/ /_//_/ \\___//_/    \\____/   " + task + "\\__/ \\__,_//____//_/|_|  " + ANSI_RESET,
				micro + "                                    " + task + "                         " + ANSI_RESET,
				ANSI_BOLD + "Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved" + ANSI_RESET,
				""
		);
	}

	@Test
	void exception_during_getVersion_prints_unknown_to_console() throws IOException {
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);

		commands.execute(printStream, "version");

		ConsoleColors.ConsoleForegroundColor micro = ConsoleColors.ConsoleForegroundColor.ANSI_FG_PURPLE;
		ConsoleColors.ConsoleForegroundColor task = ConsoleColors.ConsoleForegroundColor.ANSI_FG_CYAN;

		assertOutput(
				micro + "               _                    " + task + "   __                __  " + ANSI_RESET,
				micro + "   ____ ___   (_)_____ _____ ____   " + task + "  / /_ ____ _ _____ / /__" + ANSI_RESET,
				micro + "  / __ `__ \\ / // ___// ___// __ \\  " + task + " / __// __ `// ___// //_/" + ANSI_RESET,
				micro + " / / / / / // // /__ / /   / /_/ /  " + task + "/ /_ / /_/ /(__  )/ ,<   " + ANSI_RESET + ANSI_BOLD + "Unknown" + ANSI_RESET,
				micro + "/_/ /_/ /_//_/ \\___//_/    \\____/   " + task + "\\__/ \\__,_//____//_/|_|  " + ANSI_RESET,
				micro + "                                    " + task + "                         " + ANSI_RESET,
				ANSI_BOLD + "Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved" + ANSI_RESET,
				""
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void version_command_help(String parameter) {
		commands.execute(printStream, "version " + parameter);

		assertOutput(
				"Usage:  version [-h]",
				"  -h, --help   Show this help message."
		);
	}
}
