// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class Commands_Exit_Test extends CommandsBaseTestCase {
	@Test
	void exit_command_tells_osInterface_to_exit_the_application() {
		commands.execute(printStream, "exit");

		Mockito.verify(osInterface).exit();
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void exit_command_help(String parameter) {
		commands.execute(printStream, "exit " + parameter);

		assertOutput(
				"Usage:  exit [-h]",
				"  -h, --help   Show this help message."
		);
	}
}
