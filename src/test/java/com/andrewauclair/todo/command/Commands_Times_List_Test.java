// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskTimesFilter;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

// TODO I would like a more detailed broken down set of tests for the times command variations
class Commands_Times_List_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_on_default_list() {
		tasks.addTask("Test 1");
		tasks.startTask(1, false);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --list default");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/default");

		assertOutput(
				"Times for list '/default'",
				"",
				"1h 49m 15s F 3 - 'Test 3'",
				"1h  1m 39s   2 - 'Test 2'",
				"   32m 20s R 5 - 'Test 5'",
				"   10m 21s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ConsoleColors.ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_command_filtering_for_multiple_lists() {
		tasks.addTask("Test 1");
		tasks.startTask(1, false);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/testing"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/testing")
				)
		);

		commands.execute(printStream, "times --list default --list /testing");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/default");
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/testing");

		assertOutput(
				"Times for multiple lists",
				"",
				ConsoleColors.ANSI_BOLD + "/testing" + ConsoleColors.ANSI_RESET,
				"1h 49m 15s F 3 - 'Test 3'",
				"   32m 20s R 5 - 'Test 5'",
				"",
				ConsoleColors.ANSI_BOLD + "/default" + ConsoleColors.ANSI_RESET,
				"1h  1m 39s   2 - 'Test 2'",
				"   10m 21s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ConsoleColors.ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}
}
