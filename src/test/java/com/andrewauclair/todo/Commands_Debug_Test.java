// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Commands_Debug_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));

	@Test
	void execute_debug_enable_command() {
		commands.execute("debug enable");

		assertTrue(commands.isDebugEnabled());
	}

	@Test
	void execute_debug_disable_command() {
		commands.execute("debug enable");
		commands.execute("debug disable");
		assertFalse(commands.isDebugEnabled());
	}
	
	@Test
	void executing_debug_with_any_text_other_than_enable_or_disable_results_in_an_invalid_command_message() {
		commands.execute("debug junk");
		assertEquals("Invalid command." + System.lineSeparator(), outputStream.toString());
	}
}
