// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Active_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));

	@Test
	void execute_active_command() {
		tasks.addTask("com.andrewauclair.todo.Task 1");
		tasks.startTask(1);
		commands.execute("active");

		assertEquals("Active task is 1 - \"com.andrewauclair.todo.Task 1\"" + System.lineSeparator(), outputStream.toString());

		Mockito.verify(tasks).startTask(1);
	}
}
