// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Unknown_Test {
	private Tasks tasks = Mockito.spy(Tasks.class);
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void prints_unknown_command_when_command_isnt_found() {
		commands.execute("junk");
		
		assertEquals("Unknown command." + System.lineSeparator(), outputStream.toString());
	}
}
