// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.command.Commands;
import com.andrewauclair.microtask.command.CommandsBaseTestCase;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED;
import static org.junit.jupiter.api.Assertions.*;

class Tasks_Load_Test extends TaskBaseTestCase {
	private final DataLoader loader = Mockito.mock(DataLoader.class);
	private final Commands commands = Mockito.mock(Commands.class);

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
		tasks.addGroup(newGroup("/test/one/"));
		tasks.addList(newList("/test/one/two"), true);
		tasks.setCurrentList(existingList("/test/one/two"));
		tasks.addTask("Test");

		tasks.load(loader, commands);

		assertFalse(tasks.hasTaskWithID(1));

		TaskGroupFinder groupFinder = new TaskGroupFinder(tasks);
		assertFalse(groupFinder.hasGroupPath(new TaskGroupName(tasks, "/test/one/")));
	}

	@Test
	void tasks_sets_active_task_id_list_group() throws IOException {
		tasks.addTask("Test");
		tasks.startTask(existingID(1), false);

		Mockito.doAnswer(invocationOnMock -> {
			tasks.addList(newList("/default"), true);
			tasks.addTask(newTask(newID(1), idValidator, "Test", TaskState.Finished, 1000));
			tasks.addGroup(newGroup("/test/"));
			tasks.addList(newList("/test/data"), true);
			tasks.setCurrentList(existingList("/test/data"));
			tasks.addTask(newTask(newID(2), idValidator, "Test", TaskState.Active, 1000));
			tasks.setCurrentList(existingList("/default"));
			return true;
		}).when(loader).load();

		tasks.load(loader, commands);

		assertEquals(2, tasks.getActiveTaskID());
		assertEquals(existingList("/test/data"), tasks.getCurrentList());
		assertEquals("/test/", tasks.getCurrentGroup().getFullPath());
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
		Mockito.when(osInterface.createInputStream("git-data/next-id.txt")).thenReturn(createInputStream("2"));

		tasks.load(loader, commands);

		assertEquals(2, tasks.nextID());
	}

	@Test
	void tasks_load_resets_active_group() {
		tasks.createGroup(newGroup("/one/"));
		tasks.setCurrentGroup(existingGroup("/one/"));

		tasks.load(loader, commands);

		assertEquals("/", tasks.getCurrentGroup().getFullPath());
	}

	@Test
	@Disabled
	void tasks_load_resets_active_task() {
		tasks.addTask("Test");
		tasks.startTask(existingID(1), false);

		tasks.load(loader, commands);

		assertFalse(tasks.hasActiveTask());
		assertEquals(existingList("/default"), tasks.getCurrentList());
	}
}
