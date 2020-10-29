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

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.TestUtils.newTaskBuilder;
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
		Task task = newTask(1, "Test", TaskState.Inactive, 1234);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 606034",
				"",
				"add 1234",
				"END",
				""
		);

		assertTrue(writeTask);
	}

	@Test
	void write_task_contents_to_specific_output_stream() {
		Task task = newTask(5, "Test", TaskState.Inactive,1234);
		writer.writeTask(task, outputStream);


		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 606034",
				"",
				"add 1234",
				"END",
				""
		);
	}
	@Test
	void write_recurring_task() {
		Task task = newTask(1, "Test", TaskState.Inactive, 1000, true);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"true",
				"due 605800",
				"",
				"add 1000",
				"END",
				""
		);

		assertTrue(writeTask);
	}

	@Test
	void write_task_due_date() {
		Task task = newTaskBuilder(1, "Test", TaskState.Inactive, 1000)
				.withDueTime(5876)
				.build();

		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 5876",
				"",
				"add 1000",
				"END",
				""
		);

		assertTrue(writeTask);
	}

	@Test
	void write_task_with_project() {
		Task task = newTask(1, "Test", TaskState.Inactive, 123,
				Collections.singletonList(
						new TaskTimes(1234, 4567, "Project 1", "")
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Inactive, 123,
				Collections.singletonList(
						new TaskTimes(1234, 4567, "", "Feature 1")
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Inactive, 123,
				Collections.singletonList(
						new TaskTimes(1234, 4567, "Project 1", "Feature 1")
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Active, 1234, Collections.singletonList(new TaskTimes(2345)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Active",
				"false",
				"due 606034",
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
		Task task = newTask(1, "Test", TaskState.Inactive, 123, Collections.singletonList(new TaskTimes(1234, 4567)));
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Active, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567),
						new TaskTimes(3333)
				)
		);

		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Active",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Inactive, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567),
						new TaskTimes(3333, 5555)
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Inactive, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567, "Project 1", "Feature 1"),
						new TaskTimes(3333, 5555, "Project 2", "Feature 2")
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");

		assertOutput(
				"Test",
				"Inactive",
				"false",
				"due 604923",
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
		Task task = newTask(1, "Test", TaskState.Finished, 123, 5678,
				Collections.singletonList(
						new TaskTimes(1234, 4567)
				)
		);
		boolean writeTask = writer.writeTask(task, "git-data/1.txt");
		
		assertOutput(
				"Test",
				"Finished",
				"false",
				"due 604923",
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

		Task task = newTask(1, "Test", TaskState.Inactive, 0);
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
