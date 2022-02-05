// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.StatusConsole;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Focus_Test extends CommandsBaseTestCase {
	@Test
	void focus_command_sends_focus_message_to_status_console() {
		commands.execute(printStream, "focus");

		Mockito.verify(osInterface).sendStatusMessage(StatusConsole.TransferType.FOCUS);
	}

	@Test
	void focus_command_help() {
		commands.execute(printStream, "focus --help");

		assertOutput(
				"Usage:  focus [-h]",
				"Set the status bar to be in focus.",
				"  -h, --help   Show this help message."
		);
	}
}
