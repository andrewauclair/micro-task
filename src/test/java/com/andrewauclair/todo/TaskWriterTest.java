// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TaskWriterTest {
	private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
	private final OutputStream outputStream = new ByteArrayOutputStream();
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final TaskWriter writer = new TaskWriter(new PrintStream(consoleOutput), osInterface);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream("git-data/1.txt")).thenReturn(new DataOutputStream(outputStream));
	}

	@Test
	void write_task_contents_to_file() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1234)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL +
				"add 1234", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_issue_and_charge() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.emptyList(), 12345, "Issues");
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"12345" + Utils.NL +
				"Issues", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_start_time() {
		Task task = new Task(1, "Test", TaskState.Active, Arrays.asList(new TaskTimes(1234), new TaskTimes(2345)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + Utils.NL +
				"Active" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL + Utils.NL + // charge
				"add 1234" + Utils.NL +
				"start 2345", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_start_and_stop_times() {
		Task task = new Task(1, "Test", TaskState.Finished, Arrays.asList(new TaskTimes(123), new TaskTimes(1234, 4567)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + Utils.NL +
				"Finished" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL + Utils.NL + // charge
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_start_stop_and_start_again() {
		Task task = new Task(1, "Test", TaskState.Active,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567),
						new TaskTimes(3333)
				)
		);

		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + Utils.NL +
				"Active" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL + Utils.NL + // charge
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_multiple_starts_and_stops() {
		Task task = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567),
						new TaskTimes(3333, 5555)
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL + Utils.NL + // charge
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333" + Utils.NL +
				"stop 5555", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void thrown_exception_makes_writeTask_return_false() throws IOException {
		OSInterface osInterface = Mockito.mock(OSInterface.class);
		
		DataOutputStream outputStream = Mockito.mock(DataOutputStream.class);
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(outputStream);

		Mockito.doThrow(IOException.class).when(outputStream).write(Mockito.any());

		ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();

		TaskWriter writer = new TaskWriter(new PrintStream(consoleOutput), osInterface);

		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		assertFalse(writer.writeTask(task, "test.txt"));

		assertEquals("java.io.IOException" + Utils.NL, consoleOutput.toString());
	}
}
