// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.Utils;
import com.andrewauclair.microtask.os.OSInterface;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Versions 1-3 have been retired
// Current file version is 4
class TaskReaderTest {
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	
	@Test
	void read_task_with_just_a_start() throws IOException {
		// TODO I believe we have a better way to set this up so it's easier to read
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL +
				"add 123" + Utils.NL +
				"start 1234" + Utils.NL +
				"Project" + Utils.NL +
				"Feature";
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/tasks/default/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/tasks/default/1.txt");
		
		Task expectedTask = new Task(1, "Test", TaskState.Active,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, "Project", "Feature")
				),
				false
		);
		
		assertEquals(expectedTask, task);
	}
	
	@Test
	void read_recurring_task() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"true" + Utils.NL +
				"Project" + Utils.NL +
				"Feature" + Utils.NL +
				"add 123";
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", TaskState.Active,
				Collections.singletonList(
						new TaskTimes(123)
				),
				true
		);
		
		assertEquals(expectedTask, task);
	}
	
	@Test
	void read_task_with_only_add() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL +
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
				"" + Utils.NL +
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
				"" + Utils.NL +
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
				"stop 5555"
		);
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567, "Project 1", "Feature 1"),
						new TaskTimes(3333, 5555, "Project 2", "Feature 2")
				)
		);
		
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
				"stop 5555"
		);

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);

		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Arrays.asList(
						new TaskTimes(123),
						new TaskTimes(1234, 4567, "start", "stop"),
						new TaskTimes(3333, 5555, "Project 2", "Feature 2")
				)
		);

		assertEquals(expectedTask, task);
	}
	
	InputStream createInputStream(String... lines) {
		String content = String.join(Utils.NL, lines);
		
		return new ByteArrayInputStream(content.getBytes());
	}
	
	@Test
	void read_task_with_finish() throws IOException {
		String contents = "Test" + Utils.NL +
				"Finished" + Utils.NL +
				"false" + Utils.NL +
				"" + Utils.NL +
				"" + Utils.NL +
				"add 1234" + Utils.NL +
				"start 2345" + Utils.NL +
				"stop 3456" + Utils.NL +
				"finish 3456" + Utils.NL;
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", TaskState.Finished,
				Arrays.asList(new TaskTimes(1234), new TaskTimes(2345, 3456), new TaskTimes(3456))
		);
		
		assertEquals(expectedTask, task);
	}
}
