// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.microtask.os.ConsoleColors.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Group_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_on_single_group() {
		tasks.addList("/one/impl", true);
		tasks.addList("/one/test", true);
		tasks.addList("/two/data", true);

		tasks.setActiveList("/one/impl");
		tasks.addTask("Test 1");
		tasks.startTask(1, false);

		tasks.setActiveList("/default");
		tasks.switchGroup("/");

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/one/test")
				)
		);

		commands.execute(printStream, "times --group one/");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, "", "", TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		assertOutput(
				"Times for group '/one/'",
				"",
				ANSI_BOLD + "2h 21m 35s /one/test" + ANSI_RESET,
				"1h 49m 15s F 3 - 'Test 3'",
				"   32m 20s R 5 - 'Test 5'",
				"",
				ANSI_BOLD + "1h 12m  0s /one/impl" + ANSI_RESET,
				"1h  1m 39s   2 - 'Test 2'",
				"   10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_on_multiple_groups() {
		tasks.addList("/one/data", true);
		tasks.addList("/two/data", true);

		tasks.setActiveList("/one/data");
		tasks.addTask("Test 1");
		tasks.startTask(1, false);

		tasks.setActiveList("/default");
		tasks.switchGroup("/");

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/two/data"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/two/data")
				)
		);

		commands.execute(printStream, "times --group one/ --group /two/");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, "", "", TaskContainerState.InProgress));

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/two/"));

		assertOutput(
				"Times for multiple groups",
				"",
				ANSI_BOLD + "2h 21m 35s /two/data" + ANSI_RESET,
				"1h 49m 15s F 3 - 'Test 3'",
				"   32m 20s R 5 - 'Test 5'",
				"",
				ANSI_BOLD + "1h 12m  0s /one/data" + ANSI_RESET,
				"1h  1m 39s   2 - 'Test 2'",
				"   10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
				"",
				"3h 33m 35s   Total",
				""
		);
	}

	@Test
	void times_on_multiple_groups__total_only() {
		tasks.addList("/one/data", true);
		tasks.addList("/two/data", true);

		tasks.setActiveList("/one/data");
		tasks.addTask("Test 1");
		tasks.startTask(1, false);

		tasks.setActiveList("/default");
		tasks.switchGroup("/");

		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime), "/one/data"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime), "/two/data"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/two/data")
				)
		);

		commands.execute(printStream, "times --total --group one/ --group /two/");

		TaskGroup group = new TaskGroup("/one/");
		group.addChild(new TaskList("/one/data", group, osInterface, writer, "", "", TaskContainerState.InProgress));

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
