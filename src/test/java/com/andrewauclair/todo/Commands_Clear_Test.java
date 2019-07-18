// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Clear_Test extends CommandsBaseTestCase {
	@Test
	void clear_command_calls_os_interface_clearScreen_function() {
		commands.execute(printStream, "clear");

		Mockito.verify(osInterface).clearScreen();
	}
}
