// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_List_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_list_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2);
		
		String expected = "  0 - \"Task 1\"" + System.lineSeparator() +
				"  1 - \"Task 2\"" + System.lineSeparator() +
				"* 2 - \"Task 3\"" + System.lineSeparator();
		
		commands.execute("list");
		
		assertEquals(expected, outputStream.toString());
	}
}
