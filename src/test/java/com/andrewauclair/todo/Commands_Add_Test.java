// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.Commands;
import com.andrewauclair.todo.Tasks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class Commands_Add_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_add_command() {
		commands.execute("add \"com.andrewauclair.todo.Task 1\"");
		commands.execute("add \"com.andrewauclair.todo.Task 2\"");
		
		assertEquals("Added task 0 \"com.andrewauclair.todo.Task 1\"" + System.lineSeparator() +
				"Added task 1 \"com.andrewauclair.todo.Task 2\"" + System.lineSeparator(), outputStream.toString());
		
		Mockito.verify(tasks).addTask("com.andrewauclair.todo.Task 1");
		Mockito.verify(tasks).addTask("com.andrewauclair.todo.Task 2");
	}
}