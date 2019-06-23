// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Test for a simple times command to print out a task times list, might just be a temporary step towards bigger better features
class Commands_Times_Test {
	private final TaskWriter writer = Mockito.mock(TaskWriter.class);
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final Tasks tasks = new Tasks(writer, osInterface);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@BeforeEach
	void setup() {
		Mockito.when(osInterface.getZoneId()).thenReturn(ZoneId.of("America/Chicago"));
	}
	
	@Test
	void times_command_prints_all_task_times() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		setTime(1561078202);
		tasks.startTask(2);
		
		setTime(1561079202);
		tasks.stopTask();
		
		setTime(1561080202);
		tasks.startTask(2);
		
		setTime(1561081202);
		tasks.stopTask();
		
		setTime(1561082202);
		tasks.startTask(2);
		
		setTime(1561083202);
		tasks.stopTask();
		
		setTime(1561084202);
		tasks.startTask(2);
		
		setTime(1561085202);
		
		commands.execute("times 2");
		
		String expected = "Times for 2 - \"Test 2\"" + Utils.NL + Utils.NL +
				"06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM" + Utils.NL +
				"06/20/2019 08:23:22 PM - 06/20/2019 08:40:02 PM" + Utils.NL +
				"06/20/2019 08:56:42 PM - 06/20/2019 09:13:22 PM" + Utils.NL +
				"06/20/2019 09:30:02 PM -" + Utils.NL + Utils.NL +
				"Total time: 01h 06m 40s" + Utils.NL;
		
		// 01w 01d 01h 01m 01s
		assertEquals(expected, outputStream.toString());
	}
	
	private void setTime(long time) {
		Mockito.when(osInterface.currentSeconds()).thenReturn(time);
	}
	
	@Test
	void times_command_prints_no_task_found_if_task_does_not_exist() {
		commands.execute("times 1");
		
		assertEquals("Task not found." + Utils.NL, outputStream.toString());
	}
	
	@Test
	void times_command_prints_no_times_for_task_when_task_has_never_been_started() {
		tasks.addTask("Test");
		
		commands.execute("times 1");
		
		assertEquals("No times for task 1 - \"Test\"" + Utils.NL, outputStream.toString());
	}
	
	@Test
	void times_without_a_task_number_prints_invalid_command() {
		commands.execute("times");
		
		assertEquals("Invalid command." + Utils.NL, outputStream.toString());
	}
	
	@Test
	void times_with_too_many_arguments_prints_invalid_command() {
		commands.execute("times 1 2");
		
		assertEquals("Invalid command." + Utils.NL, outputStream.toString());
	}
}
