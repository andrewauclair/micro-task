// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TaskWriterTest {
	private final OutputStream outputStream = new ByteArrayOutputStream();
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
	private final TaskWriter writer = new TaskWriter(new PrintStream(consoleOutput), osInterface);
	
	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream("git-data/1.txt")).thenReturn(outputStream);
	}
	
	@Test
	void write_task_contents_to_file() {
		Task task = new Task(1, "Test");
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + Utils.NL + "Inactive", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_start_time() {
		Task task = new Task(1, "Test", Task.TaskState.Active, new TaskTimes(1234));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + Utils.NL +
				"Active" + Utils.NL + Utils.NL +
				"start 1234", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_start_and_stop_times() {
		Task task = new Task(1, "Test", Task.TaskState.Finished, new TaskTimes(1234, 4567));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + Utils.NL +
				"Finished" + Utils.NL + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_start_stop_and_start_again() {
		Task task = new Task(1, "Test", Task.TaskState.Active,
				Arrays.asList(
						new TaskTimes(1234, 4567),
						new TaskTimes(3333)
				)
		);
		
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + Utils.NL +
				"Active" + Utils.NL + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_multiple_starts_and_stops() {
		Task task = new Task(1, "Test", Task.TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(1234, 4567),
						new TaskTimes(3333, 5555)
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333" + Utils.NL +
				"stop 5555", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void thrown_exception_makes_writeTask_return_false() throws IOException {
		OSInterface osInterface = Mockito.mock(OSInterface.class);
		
		OutputStream outputStream = Mockito.mock(OutputStream.class);
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(outputStream);
		
		Mockito.doThrow(IOException.class).when(outputStream).write(Mockito.any());
		
		ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
		
		TaskWriter writer = new TaskWriter(new PrintStream(consoleOutput), osInterface);
		
		Task task = new Task(1, "Test");
		assertFalse(writer.writeTask(task, "test.txt"));
		
		assertEquals("java.io.IOException" + Utils.NL, consoleOutput.toString());
	}
}
