// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

class Commands_Times_Day_Test extends Commands_Times_BaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-m 6 -d 17 -y 2019", "-m 6 -d 17", "-d 17 -y 2019"})
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight(String parameters) {
		setTime(june18_8_am);
		
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
		
		commands.execute(printStream, "times " + parameters);
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for day 06/17/2019",
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
	void show_only_total() {
		setTime(june18_8_am);

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

		commands.execute(printStream, "times --total -m 6 -d 17 -y 2019");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		assertOutput(
				"Total time for day 06/17/2019",
				"",
				"3h 33m 35s   Total",
				""
		);
	}
}
