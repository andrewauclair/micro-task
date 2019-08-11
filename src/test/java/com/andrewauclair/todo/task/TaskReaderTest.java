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

class TaskReaderTest {
	private final OSInterface osInterface = Mockito.mock(OSInterface.class);

	@Test
	void read_task_with_just_a_start() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL +
				"12345" + Utils.NL +
				"Issues" + Utils.NL + Utils.NL +
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
				12345,
				"Issues"
		);

		assertEquals(expectedTask, task);
	}

	@Test
	void read_task_with_start_stop_and_start_again() throws IOException {
		String contents = "Test" + Utils.NL +
				"Active" + Utils.NL + Utils.NL +
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
				"Inactive" + Utils.NL + Utils.NL +
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
	void task_with_no_times_has_empty_times_list_after_read() throws IOException {
		String contents = "Test" + Utils.NL + "Inactive" + Utils.NL + "12345" + Utils.NL + "Issues";

		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());

		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);

		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/1.txt");

		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Collections.emptyList(),
				12345,
				"Issues"
		);

		assertEquals(expectedTask, task);
	}
	
	// this test should be kept as is, minus compile changes, this verifies that we can still read the original file format
	@Test
	void task_reader_reads_version_1_file_format_before_issues() throws IOException {
		String contents = "Test" + Utils.NL +
				"Inactive" + Utils.NL + Utils.NL +
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
						new TaskTimes(1234, 4567),
						new TaskTimes(3333, 5555)
				)
		);
		
		assertEquals(expectedTask, task);
	}
	
	@Test
	void task_reader_reads_version_1_file_with_title_an_state() throws IOException {
		String contents = "Test" + Utils.NL + "Inactive";
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Collections.emptyList()
		);
		
		assertEquals(expectedTask, task);
	}
	
	@Test
	void task_reader_reads_version_1_file_with_title_an_state_and_blank_line() throws IOException {
		String contents = "Test" + Utils.NL + "Inactive" + Utils.NL;
		
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
		
		Mockito.when(osInterface.createInputStream("git-data/1.txt")).thenReturn(inputStream);
		
		TaskReader reader = new TaskReader(osInterface);
		
		Task task = reader.readTask(1, "git-data/1.txt");
		
		Task expectedTask = new Task(1, "Test", TaskState.Inactive,
				Collections.emptyList()
		);
		
		assertEquals(expectedTask, task);
	}
}
