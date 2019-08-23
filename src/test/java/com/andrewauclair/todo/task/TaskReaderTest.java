// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Current file version is 3
class TaskReaderTest {
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);

	@Test
	void read_task_with_just_a_start() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"Project" + Utils.NL +
				"Feature" + Utils.NL + Utils.NL +
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333";

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/tasks/default/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/tasks/default/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Active,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567),
						new TaskTimes(3333)
				),
				false, "Project", "Feature"
		);

		assertEquals(expectedTask, task);
	}

	@Test
	void read_recurring_task() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"true" + Utils.NL +
				"Project" + Utils.NL +
				"Feature" + Utils.NL + Utils.NL +
				"add 123";

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Active,
				Collections.singletonList(
						new TaskTimes(123)
				),
				true, "Project", "Feature"
		);

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_only_add() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL + Utils.NL +
				"add 123";

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Active,
				Collections.singletonList(
						new TaskTimes(123)
				)
		);

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_start_stop_and_start_again() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL + Utils.NL +
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333";

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Active,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567),
						new TaskTimes(3333)
				)
		);

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_multiple_starts_and_stops() throws IOException {
		String contents = "Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL + Utils.NL +
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"stop 4567" + Utils.NL +
				"start 3333" + Utils.NL +
				"stop 5555";

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567),
						new TaskTimes(3333, 5555)
				)
		);

		assertEquals(expectedTask, task);
	}

	// Version 1 has been retired

	@Test
	void task_reader_reads_version_2_file_with_issue() throws IOException {
		String contents = "Test" + Utils.NL +
				"Inactive" + Utils.NL +
				"-1" + Utils.NL +
				"" + Utils.NL + Utils.NL +
				"add 1000" + Utils.NL;

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Collections.singletonList(new TaskTimes(1000)),
				false, "", ""
		);

		assertEquals(expectedTask, task);
	}
}
