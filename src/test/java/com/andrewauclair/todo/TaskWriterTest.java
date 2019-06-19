// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TaskWriterTest {
	@Test
	void write_task_contents_to_file() throws IOException {
		OutputStream outputStream = new ByteArrayOutputStream();
		FileCreator fileCreator = Mockito.mock(FileCreator.class);
		Mockito.when(fileCreator.createOutputStream("git-data/1.txt")).thenReturn(outputStream);

		TaskWriter writer = new TaskWriter(fileCreator);

		Task task = new Task(1, "Test");
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test", outputStream.toString());
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

		Task task = new Task(1, "Test");
		assertFalse(writer.writeTask(task, "test.txt"));
	}
}
