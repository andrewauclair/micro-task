// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TestUtils;
import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterfaceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TaskWriterTest {
	private final OutputStream outputStream = new ByteArrayOutputStream();
	private final OSInterfaceImpl osInterface = Mockito.mock(OSInterfaceImpl.class);
	private final TaskWriter writer = new TaskWriter(osInterface);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream("git-data/1.txt")).thenReturn(new DataOutputStream(outputStream));
	}

	@Test
	void write_task_contents_to_file() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1234)), false);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"",
				"add 1234"
		);
		Assertions.assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"add 1234", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_recurring_task_with_project_and_feature() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), true);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"true",
				"",
				"add 1000"
		);
		assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"true" + Utils.NL +
				"" + Utils.NL +
				"add 1000", outputStream.toString());
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_start_time() {
		Task task = new Task(1, "Test", TaskState.Active, Arrays.asList(new TaskTimes(1234), new TaskTimes(2345)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Active",
				"false",
				"",
				"add 1234",
				"start 2345"
		);
		assertEquals("Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"add 1234" + Utils.NL +
				"start 2345", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_start_and_stop_times() {
		Task task = new Task(1, "Test", TaskState.Inactive, Arrays.asList(new TaskTimes(123), new TaskTimes(1234, 4567)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"",
				"add 123",
				"start 1234",
				"stop 4567"
		);
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

		assertOutput(
				"Test",
				"Active",
				"false",
				"",
				"add 123",
				"start 1234",
				"stop 4567",
				"start 3333"
		);
		assertEquals("Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
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

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"",
				"add 123",
				"start 1234",
				"stop 4567",
				"start 3333",
				"stop 5555"
		);
		assertEquals("Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333" + Utils.NL +
				"stop 5555", outputStream.toString());
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_multiple_times_with_multiple_different_projects_and_features() {
		Task task = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567, "Project 1", "Feature 1"),
						new TaskTimes(3333, 5555, "Project 2", "Feature 2")
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"",
				"add 123",
				"start 1234",
				"Project 1",
				"Feature 1",
				"stop 4567",
				"start 3333",
				"Project 2",
				"Feature 2",
				"stop 5555"
		);
		assertTrue(writeTask);
	}
	
	@Test
	void write_task_with_finish_time() {
		Task task = new Task(1, "Test", TaskState.Finished,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567),
						new TaskTimes(5678)
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertOutput(
				"Test",
				"Finished",
				"false",
				"",
				"add 123",
				"start 1234",
				"stop 4567",
				"finish 5678"
		);
		assertTrue(writeTask);
	}
	
	@Test
	void thrown_exception_makes_writeTask_return_false() throws IOException {
		OSInterfaceImpl osInterface = Mockito.mock(OSInterfaceImpl.class);
		
		DataOutputStream outputStream = Mockito.mock(DataOutputStream.class);
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(outputStream);

		Mockito.doThrow(IOException.class).when(outputStream).write(Mockito.any());

		ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();

		System.setErr(new PrintStream(consoleOutput));

		TaskWriter writer = new TaskWriter(osInterface);

		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		assertFalse(writer.writeTask(task, "test.txt"));

		assertEquals("java.io.IOException" + Utils.NL, consoleOutput.toString());
	}

	protected void assertOutput(String... lines) {
		TestUtils.assertOutput(outputStream, lines);
	}
}
