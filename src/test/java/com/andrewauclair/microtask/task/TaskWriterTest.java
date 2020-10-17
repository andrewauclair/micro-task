// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskWriterTest {
	private final OutputStream outputStream = new ByteArrayOutputStream();
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	private final TaskWriter writer = new TaskWriter(osInterface);

	@BeforeEach
	void setup() throws IOException {
		Mockito.when(osInterface.createOutputStream("git-data/1.txt")).thenReturn(new DataOutputStream(outputStream));
	}

	@Test
	void write_task_contents_to_file() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1234)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"",
				"add 1234",
				"END",
				""
		);

		assertTrue(writeTask);
	}

	@Test
	void write_task_contents_to_specific_output_stream() {
		Task task = new Task(5, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1234)));
		writer.writeTask(task, outputStream);


		assertOutput(
				"Test",
				"Inactive",
				"false",
				"",
				"add 1234",
				"END",
				""
		);
	}
	@Test
	void write_recurring_task() {
		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(1000)), true, Collections.emptyList());
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"true",
				"",
				"add 1000",
				"END",
				""
		);

		assertTrue(writeTask);
	}

	@Test
	void write_task_with_project() {
		Task task = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567, "Project 1", "")
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
				"",
				"stop 4567",
				"END",
				""
		);
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_feature() {
		Task task = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567, "", "Feature 1")
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
				"",
				"Feature 1",
				"stop 4567",
				"END",
				""
		);
		assertTrue(writeTask);
	}

	@Test
	void write_task_with_project_and_feature() {
		Task task = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567, "Project 1", "Feature 1")
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
				"END",
				""
		);
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
				"start 2345",
				"",
				"",
				"END",
				""
		);

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
				"",
				"",
				"stop 4567",
				"END",
				""
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
				"",
				"",
				"stop 4567",
				"start 3333",
				"",
				"",
				"END",
				""
		);

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
				"",
				"",
				"stop 4567",
				"start 3333",
				"",
				"",
				"stop 5555",
				"END",
				""
		);

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
				"stop 5555",
				"END",
				""
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
				"",
				"",
				"stop 4567",
				"finish 5678",
				"END",
				""
		);
		assertTrue(writeTask);
	}
	
	@Test
	void thrown_exception_makes_writeTask_return_false() throws IOException {
		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenThrow(IOException.class);

		System.setOut(new PrintStream(outputStream));

		Task task = new Task(1, "Test", TaskState.Inactive, Collections.singletonList(new TaskTimes(0)));
		assertFalse(writer.writeTask(task, "test.txt"));

		assertOutput(
				"java.io.IOException",
				""
		);
	}

	private void assertOutput(String... lines) {
		TestUtils.assertOutput(outputStream, lines);
	}
}
