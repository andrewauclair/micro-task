// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;

class Commands_Version_Test extends CommandsBaseTestCase {
	@Test
	void execute_version_command() throws IOException {
		Mockito.when(osInterface.getVersion()).thenReturn("0.0.5");
		
		commands.execute(printStream, "version");
		
		assertOutput(
				"0.0.5",
				""
		);
	}
	
	@Test
	void exception_during_getVersion_prints_unknown_to_console() throws IOException {
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);
		
		commands.execute(printStream, "version");
		
		assertOutput(
				"Unknown",
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
