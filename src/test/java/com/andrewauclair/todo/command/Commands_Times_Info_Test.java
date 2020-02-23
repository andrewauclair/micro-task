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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Info_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_info_for_a_day__today() {
		setTime(june17_8_am);
		
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));
		
		tasks.addTask("Test 1");
		tasks.startTask(1, false);
		
		String list = "/default";
		
		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(1, "Test 1", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560772955), // add 08:02:35 AM
										new TaskTimes(1560772925, 1560775080), // start - stop 08:03:05 AM - 08:38:00 AM
										new TaskTimes(1560777314, 1560781631), // start - stop 09:15:14 AM - 10:27:11 AM
										new TaskTimes(1560781631) // finish 10:27:11 AM
								)
						), list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(2, "Test 2", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560772960), // add 08:02:40 AM
										new TaskTimes(1560775080, 1560777314), // start - stop 08:38:00 AM - 09:15:14 AM
										new TaskTimes(1560777314) // finish 09:15:14 AM
								)
						), list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(3, "Test 3", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560772970), // add 08:02:50 AM
										new TaskTimes(1560781631, 1560783900), // start - stop 10:27:11 AM - 11:05:00 AM
										new TaskTimes(1560795348, 1560798900), // start - stop 02:15:48 PM - 03:15:00 PM
										new TaskTimes(1560798900) // finish 03:15:00 PM
								)
						), list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(4, "Test 4", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560782479), // add 10:41:19 AM
										new TaskTimes(1560790861, 1560791460), // start - stop 01:01:01 PM - 01:11:00 PM
										new TaskTimes(1560791460) // finish 01:11:00 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(5, "Test 5", TaskState.Inactive,
								Collections.singletonList(
										new TaskTimes(1560782542) // add 10:42:22 AM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(6, "Test 6", TaskState.Inactive,
								Arrays.asList(
										new TaskTimes(1560783880), // add 11:04:40 AM
										new TaskTimes(1560783900, 1560787272) // start - stop 11:05:00 AM - 12:01:12 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(7, "Test 7", TaskState.Inactive,
								Arrays.asList(
										new TaskTimes(1560789635), // add 12:40:35 PM
										new TaskTimes(1560791460, 1560795348) // start - stop 01:11:00 PM - 02:15:48 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(8, "Test 8", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560789815), // add 12:43:35 PM
										new TaskTimes(1560789355, 1560790861), // start - stop 12:35:55 PM - 01:01:01 PM
										new TaskTimes(1560790861) // finish 01:01:01 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(9, "Test 9", TaskState.Inactive,
								Collections.singletonList(
										new TaskTimes(1560796235) // add 02:30:35 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(10, "Test 10", TaskState.Inactive,
								Collections.singletonList(
										new TaskTimes(1560798709) // add 03:11:49 PM
								)
						),list)
				)
		);
		
		commands.execute(printStream, "times --log --today");
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskTimesFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskTimesFilter, times(1)).filterForDay(6, 17, 2019);
		
		assertOutput(
				"Times log for 06/17/2019",
				"",
				"08:02:35 AM   Add 1 - 'Test 1'",
				"08:02:40 AM   Add 2 - 'Test 2'",
				"08:02:50 AM   Add 3 - 'Test 3'",
				"08:03:05 AM   Start 1 - 'Test 1'",
				"08:38:00 AM   Stop 1 - 'Test 1'",
				"08:38:00 AM   Start 2 - 'Test 2'",
				"09:15:14 AM   Finish 2 - 'Test 2'",
				"09:15:14 AM   Start 1 - 'Test 1'",
				"10:27:11 AM   Finish 1 - 'Test 1'",
				"10:27:11 AM   Start 3 - 'Test 3'",
				"10:41:19 AM   Add 4 - 'Test 4'",
				"10:42:22 AM   Add 5 - 'Test 5'",
				"11:04:40 AM   Add 6 - 'Test 6'",
				"11:05:00 AM   Stop 3 - 'Test 3'",
				"11:05:00 AM   Start 6 - 'Test 6'",
				"12:01:12 PM   Stop 6 - 'Test 6'",
				"12:35:55 PM   Start 8 - 'Test 8'",
				"12:40:35 PM   Add 7 - 'Test 7'",
				"12:43:35 PM   Add 8 - 'Test 8'",
				"01:01:01 PM   Finish 8 - 'Test 8'",
				"01:01:01 PM   Start 4 - 'Test 4'",
				"01:11:00 PM   Finish 4 - 'Test 4'",
				"01:11:00 PM   Start 7 - 'Test 7'",
				"02:15:48 PM   Stop 7 - 'Test 7'",
				"02:15:48 PM   Start 3 - 'Test 3'",
				"02:30:35 PM   Add 9 - 'Test 9'",
				"03:11:49 PM   Add 10 - 'Test 10'",
				"03:15:00 PM   Finish 3 - 'Test 3'",
				
				"10 added",
				" 8 started",
				" 5 finished",
				
				"01h 49m 15s F 3 - 'Test 3'",
				"01h 01m 39s   2 - 'Test 2'",
				"    32m 20s R 5 - 'Test 5'",
				"    10m 21s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ConsoleColors.ANSI_RESET,
				"",
				"03h 33m 35s   Total",
				""
		);
	}
}
