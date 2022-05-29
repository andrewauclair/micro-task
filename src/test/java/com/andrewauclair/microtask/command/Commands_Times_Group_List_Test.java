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
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Group_List_Test extends Commands_Times_BaseTestCase {
	@Test
	void filter_by_a_single_group_and_single_list_non_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");
		tasks.addTask("Test 6");
		tasks.addTask("Test 7");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, TestUtils.existingTask(existingID(3), "Test 3", TaskState.Finished, addTime).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, TestUtils.existingTask(existingID(5), "Test 5", TaskState.Inactive, addTime, true).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, TestUtils.existingTask(existingID(7), "Test 7", TaskState.Inactive, addTime).build(), "/data")
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
				ANSI_BOLD + "2h 21m 35s /one/test" + ANSI_RESET,
//				"1h 49m 15s F 3 - 'Test 3'",
//				"   32m 20s R 5 - 'Test 5'",
//				"",
				ANSI_BOLD + "1h 12m  0s /one/impl" + ANSI_RESET,
//				"1h  1m 39s   2 - 'Test 2'",
//				"   10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ANSI_RESET,
//				"",
				ANSI_BOLD + "   46m 24s /data" + ANSI_RESET,
//				"   46m 24s   7 - 'Test 7'",
				"",
				"4h 19m 59s   Total",
				""
		);
	}

	@Test
	void filter_by_a_single_group_and_single_list_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");
		tasks.addTask("Test 6");
		tasks.addTask("Test 7");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, TestUtils.existingTask(existingID(3), "Test 3", TaskState.Finished, addTime).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, TestUtils.existingTask(existingID(5), "Test 5", TaskState.Inactive, addTime, true).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, TestUtils.existingTask(existingID(7), "Test 7", TaskState.Inactive, addTime).build(), "/data")
				)
		);

		commands.execute(printStream, "times --group /one/ --list /data -v");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/data");
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for multiple lists",
				"",
				u + "Time" + r + "        " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "1h 49m 15s    F    3  Test 3     " + ANSI_RESET,
				"1h  1m 39s         2  Test 2     ",
				ANSI_BG_GRAY + "   46m 24s         7  Test 7     " + ANSI_RESET,
				"   32m 20s   R     5  Test 5     ",
				ANSI_BG_GREEN + "   10m 21s  *      1  Test 1     " + ANSI_RESET,
				"",
				"4h 19m 59s   Total",
				""
		);
	}

	@Test
	void filter_by_multiple_groups_and_multiple_lists_non_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/two/impl"), true);
		tasks.addList(newList("/two/test"), true);
		tasks.addList(newList("/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");
		tasks.addTask("Test 6");
		tasks.addTask("Test 7");
		tasks.addTask("Test 8");
		tasks.addTask("Test 9");
		tasks.addTask("Test 10");
		tasks.addTask("Test 11");
		tasks.addTask("Test 12");
		tasks.addTask("Test 13");
		tasks.addTask("Test 14");
		tasks.addTask("Test 15");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, TestUtils.existingTask(existingID(3), "Test 3", TaskState.Finished, addTime).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, TestUtils.existingTask(existingID(5), "Test 5", TaskState.Inactive, addTime, true).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(661, TestUtils.existingTask(existingID(10), "Test 10", TaskState.Inactive, addTime).build(), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3829, TestUtils.existingTask(existingID(11), "Test 11", TaskState.Inactive, addTime).build(), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6155, TestUtils.existingTask(existingID(12), "Test 12", TaskState.Finished, addTime).build(), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(3940, TestUtils.existingTask(existingID(15), "Test 15", TaskState.Inactive, addTime, true).build(), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, TestUtils.existingTask(existingID(7), "Test 7", TaskState.Inactive, addTime).build(), "/data"),
						new TaskTimesFilter.TaskTimeFilterResult(2894, TestUtils.existingTask(existingID(8), "Test 8", TaskState.Inactive, addTime).build(), "/default")
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
				ANSI_BOLD + "   2h 48m 15s /two/test" + ANSI_RESET,
//				"   1h 42m 35s F 12 - 'Test 12'",
//				"   1h  5m 40s R 15 - 'Test 15'",
//				"",
				ANSI_BOLD + "   2h 21m 35s /one/test" + ANSI_RESET,
//				"   1h 49m 15s F  3 - 'Test 3'",
//				"      32m 20s R  5 - 'Test 5'",
//				"",
				ANSI_BOLD + "   1h 14m 50s /two/impl" + ANSI_RESET,
//				"   1h  3m 49s   11 - 'Test 11'",
//				"      11m  1s   10 - 'Test 10'",
//				"",
				ANSI_BOLD + "   1h 12m  0s /one/impl" + ANSI_RESET,
//				"   1h  1m 39s    2 - 'Test 2'",
//				"      10m 21s * " + ConsoleForegroundColor.ANSI_FG_GREEN + " 1 - 'Test 1'" + ANSI_RESET,
//				"",
				ANSI_BOLD + "      48m 14s /default" + ANSI_RESET,
//				"      48m 14s    8 - 'Test 8'",
//				"",
				ANSI_BOLD + "      46m 24s /data" + ANSI_RESET,
//				"      46m 24s    7 - 'Test 7'",
				"",
				"1d 1h 11m 18s   Total",
				""
		);
	}

	@Test
	void filter_by_multiple_groups_and_multiple_lists_verbose() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/two/impl"), true);
		tasks.addList(newList("/two/test"), true);
		tasks.addList(newList("/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");
		tasks.addTask("Test 6");
		tasks.addTask("Test 7");
		tasks.addTask("Test 8");
		tasks.addTask("Test 9");
		tasks.addTask("Test 10");
		tasks.addTask("Test 11");
		tasks.addTask("Test 12");
		tasks.addTask("Test 13");
		tasks.addTask("Test 14");
		tasks.addTask("Test 15");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, TestUtils.existingTask(existingID(3), "Test 3", TaskState.Finished, addTime).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, TestUtils.existingTask(existingID(5), "Test 5", TaskState.Inactive, addTime, true).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(661, TestUtils.existingTask(existingID(10), "Test 10", TaskState.Inactive, addTime).build(), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3829, TestUtils.existingTask(existingID(11), "Test 11", TaskState.Inactive, addTime).build(), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6155, TestUtils.existingTask(existingID(12), "Test 12", TaskState.Finished, addTime).build(), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(3940, TestUtils.existingTask(existingID(15), "Test 15", TaskState.Inactive, addTime, true).build(), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, TestUtils.existingTask(existingID(7), "Test 7", TaskState.Inactive, addTime).build(), "/data"),
						new TaskTimesFilter.TaskTimeFilterResult(2894, TestUtils.existingTask(existingID(8), "Test 8", TaskState.Inactive, addTime).build(), "/default")
				)
		);

		commands.execute(printStream, "times --group /one/ --group /two/ --list /data --list /default -v");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/data");
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Times for multiple lists",
				"",
				u + "Time" + r + "           " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "   1h 49m 15s    F    3  Test 3     " + ANSI_RESET,
				"   1h 42m 35s    F   12  Test 12    ",
				ANSI_BG_GRAY + "   1h  5m 40s   R    15  Test 15    " + ANSI_RESET,
				"   1h  3m 49s        11  Test 11    ",
				ANSI_BG_GRAY + "   1h  1m 39s         2  Test 2     " + ANSI_RESET,
				"      48m 14s         8  Test 8     ",
				ANSI_BG_GRAY + "      46m 24s         7  Test 7     " + ANSI_RESET,
				"      32m 20s   R     5  Test 5     ",
				ANSI_BG_GRAY + "      11m  1s        10  Test 10    " + ANSI_RESET,
				ANSI_BG_GREEN + "      10m 21s  *      1  Test 1     " + ANSI_RESET,
				"",
				"1d 1h 11m 18s   Total",
				""
		);
	}

	@Test
	void filter_by_multiple_groups_and_multiple_lists__total_only() {
		tasks.addGroup(newGroup("/one/"));
		tasks.addGroup(newGroup("/two/"));
		tasks.addList(newList("/one/impl"), true);
		tasks.addList(newList("/one/test"), true);
		tasks.addList(newList("/two/impl"), true);
		tasks.addList(newList("/two/test"), true);
		tasks.addList(newList("/data"), true);

		tasks.setCurrentList(existingList("/one/impl"));
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");
		tasks.addTask("Test 6");
		tasks.addTask("Test 7");
		tasks.addTask("Test 8");
		tasks.addTask("Test 9");
		tasks.addTask("Test 10");
		tasks.addTask("Test 11");
		tasks.addTask("Test 12");
		tasks.addTask("Test 13");
		tasks.addTask("Test 14");
		tasks.addTask("Test 15");
		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("/default"));
		tasks.setCurrentGroup(existingGroup("/"));

		long addTime = 0;

		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(621, TestUtils.existingTask(existingID(1), "Test 1", TaskState.Active, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3699, TestUtils.existingTask(existingID(2), "Test 2", TaskState.Inactive, addTime).build(), "/one/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6555, TestUtils.existingTask(existingID(3), "Test 3", TaskState.Finished, addTime).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(1940, TestUtils.existingTask(existingID(5), "Test 5", TaskState.Inactive, addTime, true).build(), "/one/test"),
						new TaskTimesFilter.TaskTimeFilterResult(661, TestUtils.existingTask(existingID(10), "Test 10", TaskState.Inactive, addTime).build(), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(3829, TestUtils.existingTask(existingID(11), "Test 11", TaskState.Inactive, addTime).build(), "/two/impl"),
						new TaskTimesFilter.TaskTimeFilterResult(6155, TestUtils.existingTask(existingID(12), "Test 12", TaskState.Finished, addTime).build(), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(3940, TestUtils.existingTask(existingID(15), "Test 15", TaskState.Inactive, addTime, true).build(), "/two/test"),
						new TaskTimesFilter.TaskTimeFilterResult(2784, TestUtils.existingTask(existingID(7), "Test 7", TaskState.Inactive, addTime).build(), "/data"),
						new TaskTimesFilter.TaskTimeFilterResult(2894, TestUtils.existingTask(existingID(8), "Test 8", TaskState.Inactive, addTime).build(), "/default")
				)
		);

		commands.execute(printStream, "times --total --group /one/ --group /two/ --list /data --list /default");

		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForList("/data");
		order.verify(mockTaskTimesFilter, times(1)).filterForGroup(tasks.getGroup("/one/"));

		assertOutput(
				"Total times for multiple lists",
				"",
				ANSI_BOLD + "   2h 48m 15s /two/test" + ANSI_RESET,
				ANSI_BOLD + "   2h 21m 35s /one/test" + ANSI_RESET,
				ANSI_BOLD + "   1h 14m 50s /two/impl" + ANSI_RESET,
				ANSI_BOLD + "   1h 12m  0s /one/impl" + ANSI_RESET,
				ANSI_BOLD + "      48m 14s /default" + ANSI_RESET,
				ANSI_BOLD + "      46m 24s /data" + ANSI_RESET,
				"",
				"1d 1h 11m 18s   Total",
				""
		);
	}
}
