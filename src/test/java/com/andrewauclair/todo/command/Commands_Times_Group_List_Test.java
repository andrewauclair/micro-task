// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import com.andrewauclair.todo.task.TaskTimesFilter;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.andrewauclair.todo.os.ConsoleColors.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Group_List_Test extends Commands_Times_BaseTestCase {
	@Test
	void filter_by_a_single_group_and_single_list() {
		tasks.addList("/one/impl", true);
		tasks.addList("/one/test", true);
		tasks.addList("/data", true);

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
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, new Task(7, "Test 7", TaskState.Inactive, addTime), "/data")
				)
		);

		commands.execute(printStream, "times --group /one/ --list /data");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/data");
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		assertOutput(
				"Times for multiple lists",
				"",
				ANSI_BOLD + "/one/test" + ANSI_RESET,
				"01h 49m 15s F 3 - 'Test 3'",
				"    32m 20s R 5 - 'Test 5'",
				"",
				ANSI_BOLD + "/one/impl" + ANSI_RESET,
				"01h 01m 39s   2 - 'Test 2'",
				"    10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
				"",
				ANSI_BOLD + "/data" + ANSI_RESET,
				"    46m 24s   7 - 'Test 7'",
				"",
				"04h 19m 59s   Total",
				""
		);
	}

	@Test
	void filter_by_multiple_groups_and_multiple_lists() {
		tasks.addList("/one/impl", true);
		tasks.addList("/one/test", true);
		tasks.addList("/two/impl", true);
		tasks.addList("/two/test", true);
		tasks.addList("/data", true);

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
						new TaskTimesFilter.TaskTimeFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(661, new Task(10, "Test 10", TaskState.Inactive, addTime), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3829, new Task(11, "Test 11", TaskState.Inactive, addTime), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6155, new Task(12, "Test 12", TaskState.Finished, addTime), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(3940, new Task(15, "Test 15", TaskState.Inactive, addTime, true), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, new Task(7, "Test 7", TaskState.Inactive, addTime), "/data"),
						new TaskTimesFilter.TaskTimeFilterResult(2894, new Task(8, "Test 8", TaskState.Inactive, addTime), "/default")
				)
		);

		commands.execute(printStream, "times --group /one/ --group /two/ --list /data --list /default");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/data");
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		assertOutput(
				"Times for multiple lists",
				"",
				ANSI_BOLD + "/two/test" + ANSI_RESET,
				"    01h 42m 35s F 12 - 'Test 12'",
				"    01h 05m 40s R 15 - 'Test 15'",
				"",
				ANSI_BOLD + "/one/test" + ANSI_RESET,
				"    01h 49m 15s F  3 - 'Test 3'",
				"        32m 20s R  5 - 'Test 5'",
				"",
				ANSI_BOLD + "/two/impl" + ANSI_RESET,
				"    01h 03m 49s   11 - 'Test 11'",
				"        11m 01s   10 - 'Test 10'",
				"",
				ANSI_BOLD + "/one/impl" + ANSI_RESET,
				"    01h 01m 39s    2 - 'Test 2'",
				"        10m 21s *  " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
				"",
				ANSI_BOLD + "/default" + ANSI_RESET,
				"        48m 14s    8 - 'Test 8'",
				"",
				ANSI_BOLD + "/data" + ANSI_RESET,
				"        46m 24s    7 - 'Test 7'",
				"",
				"01d 01h 11m 18s   Total",
				""
		);
	}
}
