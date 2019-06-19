// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

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
		tasks.addTask("com.andrewauclair.todo.Task 1");
		tasks.addTask("com.andrewauclair.todo.Task 2");
		tasks.addTask("com.andrewauclair.todo.Task 3");
		tasks.startTask(1);
		tasks.finishTask();
		tasks.startTask(2);
		
		String expected = "  0 - \"com.andrewauclair.todo.Task 1\"" + System.lineSeparator() +
				"* 2 - \"com.andrewauclair.todo.Task 3\"" + System.lineSeparator();
		
		commands.execute("list");
		
		assertEquals(expected, outputStream.toString());
		
		//noinspection ResultOfMethodCallIgnored
		Mockito.verify(tasks).getTasks();
		Mockito.verify(tasks).getActiveTask();
	}
}
