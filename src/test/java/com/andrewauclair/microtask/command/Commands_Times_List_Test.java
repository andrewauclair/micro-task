// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_List_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_on_default_list() {
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --list default");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/default");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for list '/default'",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "1h 49m 15s    F    3  Test 3     " + ANSI_RESET,
				"1h  1m 39s         2  Test 2     ",
				ANSI_BG_GRAY + "   32m 20s   R     5  Test 5     " + ANSI_RESET,
				ANSI_BG_GREEN + "   10m 21s  *      1  Test 1     " + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_command_filtering_for_multiple_lists_without_verbose_shows_only_lists() {
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/testing"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/testing")
				)
		);

		tasks.addList(newList("/testing"), true);

		commands.execute(printStream, "times --list default --list /testing");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/default");
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/testing");

		assertOutput(
				"Times for multiple lists",
				"",
				ConsoleColors.ANSI_BOLD + "2h 21m 35s /testing" + ANSI_RESET,
				ConsoleColors.ANSI_BOLD + "1h 12m  0s /default" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_command_filtering_for_multiple_lists_verbose_displays_tasks_only() {
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/testing"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/testing")
				)
		);

		tasks.addList(newList("/testing"), true);

		commands.execute(printStream, "times --list default --list /testing -v");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/default");
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/testing");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for multiple lists",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "1h 49m 15s    F    3  Test 3     " + ANSI_RESET,
				"1h  1m 39s         2  Test 2     ",
				ANSI_BG_GRAY + "   32m 20s   R     5  Test 5     " + ANSI_RESET,
				ANSI_BG_GREEN + "   10m 21s  *      1  Test 1     " + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_command_filtering_for_multiple_lists__total_only() {
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/testing"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/testing")
				)
		);

		tasks.addList(newList("/testing"), true);

		commands.execute(printStream, "times --total --list default --list /testing");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/default");
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/testing");

		assertOutput(
				"Total times for multiple lists",
				"",
				ConsoleColors.ANSI_BOLD + "2h 21m 35s /testing" + ANSI_RESET,
				ConsoleColors.ANSI_BOLD + "1h 12m  0s /default" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}
}
