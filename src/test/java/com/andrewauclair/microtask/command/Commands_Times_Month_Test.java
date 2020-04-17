// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Month_Test extends Commands_Times_BaseTestCase {
	@Test
	void filter_for_a_month_worth_of_tasks_for_the_current_month() {
		setTime(june18_8_am);

		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --all-month");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForMonth(6);

		assertOutput(
				"Times for month of June 2019",
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
	void filter_for_a_month_worth_of_tasks_for_the_current_month__total_only() {
		setTime(june18_8_am);

		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/default")
				)
		);

		commands.execute(printStream, "times --all-month --total");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForMonth(6);

		assertOutput(
				"Total times for month of June 2019",
				"",
				"3h 33m 35s   Total",
				""
		);
	}
}
