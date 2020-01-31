// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskFilter;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class Commands_Times_Day_Test extends Commands_Times_BaseTestCase {
	@ParameterizedTest
	@ValueSource(strings = {"-m 6 -d 17 -y 2019", "-m 6 -d 17", "-d 17 -y 2019"})
	void basic_times_for_the_day__only_uses_times_from_given_day__midnight_to_midnight(String parameters) {
		setTime(june18_8_am);
		
		tasks.addTask("Test 1");
		tasks.startTask(1, false);
		
		List<TaskTimes> addTime = Collections.singletonList(new TaskTimes(0));
		
		when(mockTaskFilter.getData()).thenReturn(
				Arrays.asList(
						new TaskFilter.TaskFilterResult(621, new Task(1, "Test 1", TaskState.Active, addTime)),
						new TaskFilter.TaskFilterResult(3699, new Task(2, "Test 2", TaskState.Inactive, addTime)),
						new TaskFilter.TaskFilterResult(6555, new Task(3, "Test 3", TaskState.Finished, addTime)),
						new TaskFilter.TaskFilterResult(1940, new Task(5, "Test 5", TaskState.Inactive, addTime, true))
				)
		);
		
		commands.execute(printStream, "times --tasks " + parameters);
		
		InOrder order = Mockito.inOrder(mockTaskFilterBuilder, mockTaskFilter);
		order.verify(mockTaskFilterBuilder, times(1)).createFilter(tasks);
		order.verify(mockTaskFilter, times(1)).filterForDay(6, 17, 2019);
		
		assertOutput(
				"Times for day 06/17/2019",
				"",
				"01h 49m 15s F 3 - 'Test 3'",
				"01h 01m 39s   2 - 'Test 2'",
				"    32m 20s R 5 - 'Test 5'",
				"    10m 21s * " + ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN + "1 - 'Test 1'" + ConsoleColors.ANSI_RESET,
				"",
				"Total time: 03h 33m 35s",
				""
		);
	}
	// TODO Test that this output is cut off on the right if task name is too long, "Execute the instructions in ...", cut off at the space that fits
}
