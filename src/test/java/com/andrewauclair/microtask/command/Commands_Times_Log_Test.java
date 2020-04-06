// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.command.Commands_Times_BaseTestCase;
import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.TaskTimes;
import com.andrewauclair.microtask.task.TaskTimesFilter;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Log_Test extends Commands_Times_BaseTestCase {
	@Test
	void times_log_for_a_day__today() {
		setTime(june17_8_am);

		String list = "/default";
		
		when(mockTaskTimesFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(1, "Test 1", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560772955), // add 07:02:35 AM
										new TaskTimes(1560772985, 1560775080), // start - stop 07:03:05 AM - 07:38:00 AM
										new TaskTimes(1560777314, 1560781631), // start - stop 08:15:14 AM - 09:27:11 AM
										new TaskTimes(1560781631) // finish 09:27:11 AM
								)
						), list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(2, "Test 2", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560772960), // add 07:02:40 AM
										new TaskTimes(1560775080, 1560777314), // start - stop 07:38:00 AM - 08:15:14 AM
										new TaskTimes(1560777314) // finish 08:15:14 AM
								)
						), list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(3, "Test 3", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560772970), // add 07:02:50 AM
										new TaskTimes(1560781631, 1560783900), // start - stop 09:27:11 AM - 10:05:00 AM
										new TaskTimes(1560795348, 1560798900), // start - stop 01:15:48 PM - 02:15:00 PM
										new TaskTimes(1560798900) // finish 02:15:00 PM
								)
						), list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(4, "Test 4", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560782479), // add 09:41:19 AM
										new TaskTimes(1560790861, 1560791460), // start - stop 12:01:01 PM - 12:11:00 PM
										new TaskTimes(1560791460) // finish 12:11:00 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(5, "Test 5", TaskState.Inactive,
								Collections.singletonList(
										new TaskTimes(1560782542) // add 09:42:22 AM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(6, "Test 6", TaskState.Inactive,
								Arrays.asList(
										new TaskTimes(1560783880), // add 10:04:40 AM
										new TaskTimes(1560783900, 1560787272) // start - stop 10:05:00 AM - 11:01:12 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(7, "Test 7", TaskState.Inactive,
								Arrays.asList(
										new TaskTimes(1560789635), // add 11:40:35 PM
										new TaskTimes(1560791460, 1560795348) // start - stop 12:11:00 PM - 01:15:48 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(8, "Test 8", TaskState.Finished,
								Arrays.asList(
										new TaskTimes(1560789815), // add 11:43:35 PM
										new TaskTimes(1560789355, 1560790861), // start - stop 11:35:55 PM - 12:01:01 PM
										new TaskTimes(1560790861) // finish 12:01:01 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(9, "Test 9", TaskState.Inactive,
								Collections.singletonList(
										new TaskTimes(1560796235) // add 01:30:35 PM
								)
						),list),
						new TaskTimesFilter.TaskTimeFilterResult(0, new Task(10, "Test 10", TaskState.Inactive,
								Collections.singletonList(
										new TaskTimes(1560798709) // add 02:11:49 PM
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
				"07:02:35 AM   Added 1 - 'Test 1'",
				"07:02:40 AM   Added 2 - 'Test 2'",
				"07:02:50 AM   Added 3 - 'Test 3'",
				"07:03:05 AM   Started 1 - 'Test 1'",
				"07:38:00 AM   Stopped 1 - 'Test 1'",
				"07:38:00 AM   Started 2 - 'Test 2'",
				"08:15:14 AM   Finished 2 - 'Test 2'",
				"08:15:14 AM   Started 1 - 'Test 1'",
				"09:27:11 AM   Finished 1 - 'Test 1'",
				"09:27:11 AM   Started 3 - 'Test 3'",
				"09:41:19 AM   Added 4 - 'Test 4'",
				"09:42:22 AM   Added 5 - 'Test 5'",
				"10:04:40 AM   Added 6 - 'Test 6'",
				"10:05:00 AM   Stopped 3 - 'Test 3'",
				"10:05:00 AM   Started 6 - 'Test 6'",
				"11:01:12 PM   Stopped 6 - 'Test 6'",
				"11:35:55 PM   Started 8 - 'Test 8'",
				"11:40:35 PM   Added 7 - 'Test 7'",
				"11:43:35 PM   Added 8 - 'Test 8'",
				"12:01:01 PM   Finished 8 - 'Test 8'",
				"12:01:01 PM   Started 4 - 'Test 4'",
				"12:11:00 PM   Finished 4 - 'Test 4'",
				"12:11:00 PM   Started 7 - 'Test 7'",
				"01:15:48 PM   Stopped 7 - 'Test 7'",
				"01:15:48 PM   Started 3 - 'Test 3'",
				"01:30:35 PM   Added 9 - 'Test 9'",
				"02:11:49 PM   Added 10 - 'Test 10'",
				"02:15:00 PM   Finished 3 - 'Test 3'",
//				"",
//				"10 added",
//				" 8 started",
//				" 5 finished",
				""
		);
	}
}