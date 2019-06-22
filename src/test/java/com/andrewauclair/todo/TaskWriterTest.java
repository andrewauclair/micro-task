// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TaskWriterTest {
	private static final String NL = System.lineSeparator();
	private OutputStream outputStream = new ByteArrayOutputStream();
	//	private FileCreator fileCreator = Mockito.mock(FileCreator.class);
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);

	@BeforeEach
	void setup() throws IOException {
//		Mockito.when(fileCreator.createOutputStream("git-data/1.txt")).thenReturn(outputStream);
		Mockito.when(osInterface.createOutputStream("git-data/1.txt")).thenReturn(outputStream);
	}

	@Test
	void write_task_contents_to_file() {
		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test");
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + NL + "Inactive", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_start_time() {
		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test", Task.TaskState.Active, new TaskTimes.Times(1234));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + NL +
				"Active" + NL + NL +
				"start 1234", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_start_and_stop_times() {
		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test", Task.TaskState.Finished, new TaskTimes.Times(1234, 4567));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + NL +
				"Finished" + NL + NL +
				"start 1234" + NL +
				"stop 4567", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_start_stop_and_start_again() {
		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test", Task.TaskState.Active,
				Arrays.asList(
						new TaskTimes.Times(1234, 4567),
						new TaskTimes.Times(3333)
				)
		);

		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + NL +
				"Active" + NL + NL +
				"start 1234" + NL +
				"stop 4567" + NL +
				"start 3333", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_multiple_starts_and_stops() {
		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test", Task.TaskState.Inactive,
				Arrays.asList(
						new TaskTimes.Times(1234, 4567),
						new TaskTimes.Times(3333, 5555)
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + NL +
				"Inactive" + NL + NL +
				"start 1234" + NL +
				"stop 4567" + NL +
				"start 3333" + NL +
				"stop 5555", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void thrown_exception_makes_writeTask_return_false() {
		OSInterface osInterface = new OSInterface() {
			@Override
			public OutputStream createOutputStream(String fileName) throws IOException {
				throw new IOException();
			}
		};

		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test");
		assertFalse(writer.writeTask(task, "test.txt"));
	}
}
