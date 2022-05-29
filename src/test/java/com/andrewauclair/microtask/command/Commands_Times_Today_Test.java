// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
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
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Today_Test extends Commands_Times_BaseTestCase {
	@Test
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight() {
		setTime(june17_8_am);
		
		long addTime = 0;

		tasks.addTask("Test 1");
		tasks.addTask(newTask(newID(10), idValidator, "Test 1", TaskState.Active, addTime));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(10), "Test 1", TaskState.Active, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(newID(20), idValidator, "Test 2", TaskState.Inactive, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(30), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(500), idValidator, "Testing a longer name in the times command because it's not done anywhere else and there's a bug right now", TaskState.Inactive, addTime, true).build(), "/default")
				)
		);

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(80);

		commands.execute(printStream, "times --today");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for day 06/17/2019",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "   " + u + "Description" + r,
				ANSI_BG_GRAY + "1h 49m 15s    F    30  Test 3                                                  " + ANSI_RESET,
				"1h  1m 39s         20  Test 2                                                  ",
				ANSI_BG_GRAY + "   32m 20s   R    500  Testing a longer name in the times command because it..." + ANSI_RESET,
				ANSI_BG_GREEN + "   10m 21s  *      10  Test 1                                                  " + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}
	
	@Test
	void today_for_single_list_non_verbose() {
		setTime(june17_8_am);
		
		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/one/design")
				)
		);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/design"), true);
		
		commands.execute(printStream, "times --today --list /one/design");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		assertOutput(
				"Times for day 06/17/2019",
				"",
				ANSI_BOLD + "2h 21m 35s /one/design" + ANSI_RESET,
//				"1h 49m 15s F 3 - 'Test 3'",
//				"   32m 20s R 5 - 'Test 5'",
				"",
				"2h 21m 35s   Total",
				""
		);
	}

	@Test
	void today_for_single_list_verbose() {
		setTime(june17_8_am);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/one/design")
				)
		);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/design"), true);

		commands.execute(printStream, "times --today --list /one/design -v");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for day 06/17/2019",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "1h 49m 15s    F    3  Test 3     " + ANSI_RESET,
				"   32m 20s   R     5  Test 5     ",
				"",
				"2h 21m 35s   Total",
				""
		);
	}

	@Test
	void specific_day_for_single_list_non_verbose() {
		setTime(june17_8_am);
		
		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/one/design")
				)
		);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/design"), true);
		
		commands.execute(printStream, "times -d 17 --list /one/design");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);
		
		assertOutput(
				"Times for day 06/17/2019",
				"",
				ANSI_BOLD + "2h 21m 35s /one/design" + ANSI_RESET,
//				"1h 49m 15s F 3 - 'Test 3'",
//				"   32m 20s R 5 - 'Test 5'",
				"",
				"2h 21m 35s   Total",
				""
		);
	}

	@Test
	void specific_day_for_single_list_verbose() {
		setTime(june17_8_am);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/one/design")
				)
		);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/design"), true);

		commands.execute(printStream, "times -d 17 --list /one/design -v");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for day 06/17/2019",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "1h 49m 15s    F    3  Test 3     " + ANSI_RESET,
				"   32m 20s   R     5  Test 5     ",
				"",
				"2h 21m 35s   Total",
				""
		);
	}

	@Test
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight__total_only() {
		setTime(june17_8_am);

		long addTime = 0;

		tasks.addTask("Test 1");
		tasks.addTask(newTask(newID(10), idValidator, "Test 1", TaskState.Active, addTime));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(10), "Test 1", TaskState.Active, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(newID(20), idValidator, "Test 2", TaskState.Inactive, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(30), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/default"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(500), idValidator, "Testing a longer name in the times command because it's not done anywhere else and there's a bug right now", TaskState.Inactive, addTime, true).build(), "/default")
				)
		);

		commands.execute(printStream, "times --total --today");

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

	@Test
	void today_for_single_list__total_only() {
		setTime(june17_8_am);

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(newID(3), idValidator, "Test 3", TaskState.Finished, addTime).build(), "/one/design"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(newID(5), idValidator, "Test 5", TaskState.Inactive, addTime, true).build(), "/one/design")
				)
		);

		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/design"), true);
		
		commands.execute(printStream, "times --total --today --list /one/design");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/one/design");
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);

		assertOutput(
				"Total time for day 06/17/2019",
				"",
				ANSI_BOLD + "2h 21m 35s /one/design" + ANSI_RESET,
				"",
				"2h 21m 35s   Total",
				""
		);
	}
}
