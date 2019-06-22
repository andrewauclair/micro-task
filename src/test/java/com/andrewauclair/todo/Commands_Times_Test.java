// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Test for a simple times command to print out a task times list, might just be a temporary step towards bigger better features
class Commands_Times_Test {
	private static final String NL = System.lineSeparator();
	
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final Tasks tasks = new Tasks(writer, osInterface);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void times_command_prints_all_task_times() {
		tasks.addTask("Test");
		
		setTime(1561078202);
		tasks.startTask(1);
		setTime(1561079202);
		tasks.stopTask();
		setTime(1561080202);
		tasks.startTask(1);
		setTime(1561081202);
		tasks.stopTask();
		setTime(1561082202);
		tasks.startTask(1);
		setTime(1561083202);
		tasks.stopTask();
		setTime(1561084202);
		tasks.startTask(1);
		
		commands.execute("times 1");
		
		String expected = "Times for 1 - \"Test\"" + NL + NL +
				"06/20/2019 08:50:02 PM - 06/20/2019 09:06:42 PM" + NL +
				"06/20/2019 09:23:22 PM - 06/20/2019 09:40:02 PM" + NL +
				"06/20/2019 09:56:42 PM - 06/20/2019 10:13:22 PM" + NL +
				"06/20/2019 10:30:02 PM -" + NL;
		
		assertEquals(expected, outputStream.toString());
	}
	
	private void setTime(long time) {
		Mockito.when(osInterface.currentSeconds()).thenReturn(time);
	}
	
	@Test
	void times_command_prints_no_task_found_if_task_does_not_exist() {
		commands.execute("times 1");
		
		assertEquals("Task not found." + NL, outputStream.toString());
	}
	
	@Test
	void times_command_prints_no_times_for_task_when_task_has_never_been_started() {
		tasks.addTask("Test");
		
		commands.execute("times 1");
		
		assertEquals("No times for task 1 - \"Test\"" + NL, outputStream.toString());
	}
}
