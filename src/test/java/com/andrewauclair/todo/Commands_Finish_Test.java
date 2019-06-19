// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Finish_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_remove_command() {
		tasks.addTask("com.andrewauclair.todo.Task 1");
		tasks.startTask(0);
		commands.execute("finish");
		
		assertEquals("Finished task 0 - \"com.andrewauclair.todo.Task 1\"" + System.lineSeparator(), outputStream.toString());
		
		Mockito.verify(tasks).finishTask();
	}
}
