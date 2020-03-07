// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.util.Collections;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Load_Test extends TaskBaseTestCase {
	private TaskLoader loader = Mockito.mock(TaskLoader.class);
	private Commands commands = Mockito.mock(Commands.class);

	@Test
	void load_tasks_from_disk() throws IOException {
		tasks.load(loader, commands);

		Mockito.verify(loader).load();
	}

	@Test
	void load_aliases_from_disk() {
		tasks.load(loader, commands);

		Mockito.verify(commands).loadAliases();
	}

	@Test
	void tasks_load_prints_error_when_load_fails() throws IOException {
		Mockito.doThrow(IOException.class).when(loader).load();

		Mockito.when(osInterface.getLastInputFile()).thenReturn("1.txt");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));

		tasks.load(loader, commands);

		CommandsBaseTestCase.assertOutput(out,
				ANSI_FG_RED + "Failed to read tasks." + ANSI_RESET,
				"java.io.IOException",
				"",
				"Last file: 1.txt"
		);
	}

	@Test
	void tasks_load_prints_error_when_task_exception_is_thrown() throws IOException {
		Mockito.doThrow(TaskException.class).when(loader).load();

		Mockito.when(osInterface.getLastInputFile()).thenReturn("1.txt");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));

		tasks.load(loader, commands);

		CommandsBaseTestCase.assertOutput(out,
				ANSI_FG_RED + "Failed to read tasks." + ANSI_RESET,
				"com.andrewauclair.microtask.TaskException",
				"",
				"Last file: 1.txt"
		);
	}

	@Test
	void tasks_load_returns_true_when_load_succeeded() {
		assertTrue(tasks.load(loader, commands));
	}

	@Test
	void tasks_clears_all_data_before_loading() {
		tasks.addList("/test/one/two", true);
		tasks.setActiveList("/test/one/two");
		tasks.addTask("Test");

		tasks.load(loader, commands);

		assertFalse(tasks.hasTaskWithID(1));
		assertFalse(tasks.hasGroupPath("/test/one"));
	}

	@Test
	void tasks_sets_active_task_id_list_group() throws IOException {
		tasks.addTask("Test");
		tasks.startTask(1, false);
		
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				tasks.addList("/default", true);
				tasks.addTask(new Task(1, "Test", TaskState.Finished, Collections.singletonList(new TaskTimes(1000))));
				tasks.addList("/test/data", true);
				tasks.setActiveList("/test/data");
				tasks.addTask(new Task(2, "Test", TaskState.Active, Collections.singletonList(new TaskTimes(1000))));
				tasks.setActiveList("/default");
				return true;
			}
		}).when(loader).load();
		
		tasks.load(loader, commands);
		
		assertEquals(2, tasks.getActiveTaskID());
		assertEquals("/test/data", tasks.getActiveList());
		assertEquals("/test/", tasks.getActiveGroup().getFullPath());
	}
	
	@Test
	void tasks_load_returns_false_when_load_failed() throws IOException {
		Mockito.doThrow(TaskException.class).when(loader).load();

		assertFalse(tasks.load(loader, commands));
	}

	@Test
	void tasks_load_defaults_next_id_to_1() {
		tasks.load(loader, commands);

		assertEquals(1, tasks.nextID());
	}

	@Test
	void tasks_loads_the_next_id_text_file() throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream("2".getBytes());

		Mockito.when(osInterface.createInputStream("git-data/next-id.txt")).thenReturn(new DataInputStream(inputStream));

		tasks.load(loader, commands);

		assertEquals(2, tasks.nextID());
	}

	@Test
	void tasks_load_resets_active_group() {
		tasks.createGroup("/one/");
		tasks.switchGroup("/one/");

		tasks.load(loader, commands);

		assertEquals("/", tasks.getActiveGroup().getFullPath());
	}

	@Test
	void tasks_load_resets_active_task() {
		tasks.addTask("Test");
		tasks.startTask(1, false);

		tasks.load(loader, commands);

		assertFalse(tasks.hasActiveTask());
		assertEquals("/default", tasks.getActiveList());
	}
}
