// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.StatusConsole;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class Commands_Exit_Test extends CommandsBaseTestCase {
	@Test
	void exit_command_tells_osInterface_to_exit_the_application() {
		commands.execute(printStream, "exit");

		InOrder inOrder = Mockito.inOrder(osInterface);

		inOrder.verify(osInterface).sendStatusMessage(StatusConsole.TransferType.EXIT);
		inOrder.verify(osInterface).exit();
	}

	@Test
	void exit_command_help() {
		commands.execute(printStream, "exit --help");

		assertOutput(
				"Usage:  exit [-h]",
				"Exit the application.",
				"  -h, --help   Show this help message."
		);
	}
}
