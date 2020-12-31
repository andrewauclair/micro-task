// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.os.ConsoleColors.*;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Group_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_on_single_group_non_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/two/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/one/test")
				)
		);

		commands.execute(printStream, "times --group one/");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		assertOutput(
				"Times for group '/one/'",
				"",
				ANSI_BOLD + "2h 21m 35s /one/test" + ANSI_RESET,
//				"1h 49m 15s F 3 - 'Test 3'",
//				"   32m 20s R 5 - 'Test 5'",
//				"",
				ANSI_BOLD + "1h 12m  0s /one/impl" + ANSI_RESET,
//				"1h  1m 39s   2 - 'Test 2'",
//				"   10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_on_single_group_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/two/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/one/test")
				)
		);

		commands.execute(printStream, "times --group one/ -v");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for group '/one/'",
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
	void times_on_multiple_groups_non_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/data"), true);
		tasks.addList(newList("/two/data"), true);

		tasks.setCurrentList(existingList("/one/data"));
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/two/data"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/two/data")
				)
		);

		commands.execute(printStream, "times --group one/ --group /two/");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/two/"));

		assertOutput(
				"Times for multiple groups",
				"",
				ANSI_BOLD + "2h 21m 35s /two/data" + ANSI_RESET,
//				"1h 49m 15s F 3 - 'Test 3'",
//				"   32m 20s R 5 - 'Test 5'",
//				"",
				ANSI_BOLD + "1h 12m  0s /one/data" + ANSI_RESET,
//				"1h  1m 39s   2 - 'Test 2'",
//				"   10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_on_multiple_groups_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/data"), true);
		tasks.addList(newList("/two/data"), true);

		tasks.setCurrentList(existingList("/one/data"));
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/two/data"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/two/data")
				)
		);

		commands.execute(printStream, "times --group one/ --group /two/ -v");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/two/"));

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for multiple groups",
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
	void times_on_multiple_groups__total_only() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/data"), true);
		tasks.addList(newList("/two/data"), true);

		tasks.setCurrentList(existingList("/one/data"));
		tasks.addTask("Test 1");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, newTask(1, "Test 1", TaskState.Active, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, newTask(2, "Test 2", TaskState.Inactive, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, newTask(3, "Test 3", TaskState.Finished, addTime), "/two/data"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, newTask(5, "Test 5", TaskState.Inactive, addTime, true), "/two/data")
				)
		);

		commands.execute(printStream, "times --total --group one/ --group /two/");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/two/"));

		assertOutput(
				"Total times for multiple groups",
				"",
				ANSI_BOLD + "2h 21m 35s /two/data" + ANSI_RESET,
				ANSI_BOLD + "1h 12m  0s /one/data" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}
}
