// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Exit_Test extends CommandsBaseTestCase {
	@Test
	void exit_command_tells_osInterface_to_exit_the_application() {
		commands.execute("exit");

		Mockito.verify(osInterface).exit();
	}
}
