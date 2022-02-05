// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Commands_Debug_Test extends CommandsBaseTestCase {
	@Test
	void execute_debug_enable_command() {
		commands.execute(printStream, "debug --enable");
		
		Mockito.verify(localSettings).setDebugEnabled(true);
	}

	@Test
	void execute_debug_disable_command() {
		commands.execute(printStream, "debug --disable");

		Mockito.verify(localSettings).setDebugEnabled(false);
	}
}
