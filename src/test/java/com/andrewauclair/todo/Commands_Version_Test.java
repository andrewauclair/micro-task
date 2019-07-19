package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
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
}
