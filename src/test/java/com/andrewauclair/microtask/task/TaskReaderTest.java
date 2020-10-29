// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Versions 1-3 have been retired
// Current file version is 4
class TaskReaderTest {
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);

	@Test
	void read_task_with_just_a_start() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Active",
				"false",
				"",
				"",
				"add 123",
				"start 1234",
				"Project",
				"Feature",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/tasks/default/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/tasks/default/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Active, 123,
				Collections.singletonList(
						new TaskTimes(1234, "Project", "Feature")
				)
		).withDueTime(0).build();

		assertEquals(expectedTask, task);
	}

	@Test
	void read_recurring_task() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Active",
				"true",
				"Project",
				"Feature",
				"add 123",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Active, 123)
				.withRecurring(true)
				.withDueTime(0)
				.build();

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_due_date() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Active",
				"false",
				"due 1234567",
				"",
				"",
				"add 123",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Active, 123)
				.withDueTime(1234567)
				.build();

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_only_add() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Active",
				"false",
				"",
				"",
				"add 123",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Active, 123)
				.withDueTime(0).build();

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_start_stop_and_start_again() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Active",
				"false",
				"",
				"",
				"add 123",
				"start 1234",
				"",
				"",
				"stop 4567",
				"start 3333",
				"",
				"",
				"",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Active, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567),
						new TaskTimes(3333)
				)
		).withDueTime(0).build();

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_multiple_starts_and_stops() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Inactive",
				"false",
				"",
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
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Inactive, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567),
						new TaskTimes(3333, 5555)
				)
		).withDueTime(0).build();

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_task_times_with_project_and_feature() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Inactive",
				"false",
				"",
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
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Inactive, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567, "Project 1", "Feature 1"),
						new TaskTimes(3333, 5555, "Project 2", "Feature 2")
				)
		).withDueTime(0).build();

		assertEquals(expectedTask, task);
	}

	@Test
	@Disabled("Disabled until we address issue #305 in release 20.4.19")
	void read_task_with_project_feature_that_match_other_known_strings() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Inactive",
				"false",
				"",
				"",
				"add 123",
				"start 1234",
				"start", // project
				"stop", // feature
				"stop 4567",
				"start 3333",
				"Project 2",
				"Feature 2",
				"stop 5555",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTask(1, "Test", TaskState.Inactive, 123,
				Arrays.asList(
						new TaskTimes(1234, 4567, "start", "stop"),
						new TaskTimes(3333, 5555, "Project 2", "Feature 2")
				)
		);

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_finish() throws IOException {
		InputStream inputStream = createInputStream(
				"Test",
				"Finished",
				"false",
				"",
				"",
				"add 1234",
				"start 2345",
				"",
				"",
				"stop 3456",
				"finish 3456",
				"END"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = newTaskBuilder(1, "Test", TaskState.Finished, 1234, 3456,
				Collections.singletonList(new TaskTimes(2345, 3456))
		).withDueTime(0).build();

		assertEquals(expectedTask, task);
	}
}
