// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.Commands;
import com.andrewauclair.todo.Tasks;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_start_command() {
		tasks.addTask("com.andrewauclair.todo.Task 1");
		commands.execute("start 0");

		assertEquals("Started task 0 - \"com.andrewauclair.todo.Task 1\"" + System.lineSeparator(), outputStream.toString());
	}
}