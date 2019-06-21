// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskReaderTest {
	private static final String NL = System.lineSeparator();
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);
	
	@Test
	void read_task_with_just_a_start() throws IOException {
		String contents = "Test" + NL +
				"Active" + NL + NL +
				"start 1234" + NL +
				"stop 4567" + NL +
				"start 3333";
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask("git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", Task.TaskState.Active, new TaskTimes(
				Arrays.asList(new TaskTimes.Times(1234, 4567),
						new TaskTimes.Times(3333)))
		);
		
		assertEquals(expectedTask, task);
	}
	
	@Test
	void read_task_with_start_stop_and_start_again() throws IOException {
		String contents = "Test" + NL +
				"Active" + NL + NL +
				"start 1234" + NL +
				"stop 4567" + NL +
				"start 3333";
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask("git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", Task.TaskState.Active, new TaskTimes(
				Arrays.asList(new TaskTimes.Times(1234, 4567),
						new TaskTimes.Times(3333)))
		);
		
		assertEquals(expectedTask, task);
	}
	
	@Test
	void read_task_with_multiple_starts_and_stops() throws IOException {
		String contents = "Test" + NL +
				"Inactive" + NL + NL +
				"start 1234" + NL +
				"stop 4567" + NL +
				"start 3333" + NL +
				"stop 5555";
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask("git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", Task.TaskState.Inactive, new TaskTimes(
				Arrays.asList(new TaskTimes.Times(1234, 4567),
						new TaskTimes.Times(3333, 5555)))
		);
		
		assertEquals(expectedTask, task);
	}
}
