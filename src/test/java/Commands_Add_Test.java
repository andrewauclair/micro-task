// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class Commands_Add_Test {
	private Tasks tasks = Mockito.spy(Tasks.class);
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_add_command() {
		commands.execute("add \"Task 1\"");
		commands.execute("add \"Task 2\"");
		
		assertEquals("Added task 0 \"Task 1\"" + System.lineSeparator() +
				"Added task 1 \"Task 2\"" + System.lineSeparator(), outputStream.toString());
		
		Mockito.verify(tasks).addTask("Task 1");
		Mockito.verify(tasks).addTask("Task 2");
	}
}
