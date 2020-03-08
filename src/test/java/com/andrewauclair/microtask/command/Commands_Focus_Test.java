// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.StatusConsole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class Commands_Focus_Test extends CommandsBaseTestCase {
	@Test
	void focus_command_sends_focus_message_to_status_console() {
		commands.execute(printStream, "focus");

		Mockito.verify(osInterface).sendStatusMessage(StatusConsole.TransferType.Focus);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-h", "--help"})
	void focus_command_help(String parameter) {
		commands.execute(printStream, "focus " + parameter);

		assertOutput(
				"Usage:  focus [-h]",
				"  -h, --help   Show this help message."
		);
	}
}
