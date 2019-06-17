// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test {
	private Tasks tasks = Mockito.spy(Tasks.class);
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_start_command() {
		tasks.addTask("Task 1");
		commands.execute("start 0");

		assertEquals("Started task 0 - \"Task 1\"" + System.lineSeparator(), outputStream.toString());
	}
}
