// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Today_Test extends Commands_Times_BaseTestCase {
	@Test
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight() {
		setTime(june17_8_am);
		
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));
		
		tasks.addTask("Test 1");
		tasks.addTask(new Task(10, "Test 1", TaskState.Active, addTime));
//		tasks.startTask(10, false);
		
		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(10, "Test 1", TaskState.Active, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(20, "Test 2", TaskState.Inactive, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(30, "Test 3", TaskState.Finished, addTime), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(500, "Testing a longer name in the times command because it's not done anywhere else and there's a bug right now", TaskState.Inactive, addTime, true), "/default")
				)
		);
		
		commands.execute(printStream, "times --today");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);
		
		assertOutput(
				"Times for day 06/17/2019",
				"",
				"1h 49m 15s F  30 - 'Test 3'",
				"1h  1m 39s    20 - 'Test 2'",
				"   32m 20s R 500 - 'Testing a longer name in the times c...'",
				"   10m 21s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + " 10 - 'Test 1'" + ConsoleColors.ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}
	
	@Test
	void today_for_single_list() {
		setTime(june17_8_am);
		
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));
		
		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/one/design")
				)
		);
		
		commands.execute(printStream, "times --today --list /one/design");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);
		
		assertOutput(
				"Times for day 06/17/2019",
				"",
				ANSI_BOLD + "2h 21m 35s /one/design" + ANSI_RESET,
				"1h 49m 15s F 3 - 'Test 3'",
				"   32m 20s R 5 - 'Test 5'",
				"",
				"2h 21m 35s   Total",
				""
		);
	}
	
	@Test
	void specific_day_for_single_list() {
		setTime(june17_8_am);
		
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));
		
		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/one/design")
				)
		);
		
		commands.execute(printStream, "times -d 17 --list /one/design");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);
		
		assertOutput(
				"Times for day 06/17/2019",
				"",
				ANSI_BOLD + "2h 21m 35s /one/design" + ANSI_RESET,
				"1h 49m 15s F 3 - 'Test 3'",
				"   32m 20s R 5 - 'Test 5'",
				"",
				"2h 21m 35s   Total",
				""
		);
	}
}
