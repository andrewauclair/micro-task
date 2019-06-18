// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Stop_Test {
	private Tasks tasks = Mockito.spy(Tasks.class);
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_stop_command() {
		tasks.addTask("Task 1");
		tasks.startTask(0);
		commands.execute("stop");
		
		assertEquals("Stopped task 0 - \"Task 1\"" + System.lineSeparator(), outputStream.toString());
	}
}
