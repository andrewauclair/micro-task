// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TaskWriterTest {
	private static final String NL = System.lineSeparator();
	private OutputStream outputStream = new ByteArrayOutputStream();
	private FileCreator fileCreator = Mockito.mock(FileCreator.class);
	
	@BeforeEach
	void setup() throws IOException {
		Mockito.when(fileCreator.createOutputStream("git-data/1.txt")).thenReturn(outputStream);
	}
	
	@Test
	void write_task_contents_to_file() {
		TaskWriter writer = new TaskWriter(fileCreator);
		
		Task task = new Task(1, "Test", Task.TaskState.Inactive);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + NL + "Inactive", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_start_time() {
		TaskWriter writer = new TaskWriter(fileCreator);
		
		Task task = new Task(1, "Test", Task.TaskState.Active, new TaskTimes.Times(1234));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertEquals("Test" + NL +
				"Active" + NL + NL +
				"start 1234", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void thrown_exception_makes_writeTask_return_false() {
		FileCreator fileCreator = new FileCreator() {
			@Override
			OutputStream createOutputStream(String fileName) throws IOException {
				throw new IOException();
			}
		};
		
		TaskWriter writer = new TaskWriter(fileCreator);
		
		Task task = new Task(1, "Test", Task.TaskState.Inactive);
		assertFalse(writer.writeTask(task, "test.txt"));
	}
}
