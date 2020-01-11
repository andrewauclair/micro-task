// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Exit_Test extends CommandsBaseTestCase {
	@Test
	void exit_command_tells_osInterface_to_exit_the_application() {
		commands.execute(printStream, "exit");

		Mockito.verify(osInterface).exit();
	}
}
