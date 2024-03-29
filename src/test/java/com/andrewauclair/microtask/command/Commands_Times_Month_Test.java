// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.TestUtils;
import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Month_Test extends Commands_Times_BaseTestCase {
	@Test
	void filter_for_a_month_worth_of_tasks_for_the_current_month() {
		setTime(june18_8_am);

		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(newID(2), idValidator, "Test 2", TaskState.Inactive, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/default")
				)
		);

		commands.execute(printStream, "times --all-month");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForMonth(6);

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for month of June 2019",
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
	void filter_for_a_month_worth_of_tasks_for_the_current_month__total_only() {
		setTime(june18_8_am);

		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(newID(2), idValidator, "Test 2", TaskState.Inactive, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/default")
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
