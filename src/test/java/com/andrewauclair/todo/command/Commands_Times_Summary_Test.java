// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.TaskState;
import com.andrewauclair.todo.task.TaskTimes;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class Commands_Times_Summary_Test extends CommandsBaseTestCase {
	@Test
	void times_summary() {
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(0), new TaskTimes(0, 3590))));
		tasks.addTask(new Task(2, "Test 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(0))));
		tasks.addTask(new Task(3, "Test 3", TaskState.Inactive, Arrays.asList(new TaskTimes(0), new TaskTimes(0))));
		tasks.addTask(new Task(4, "Test 4", TaskState.Inactive, Arrays.asList(new TaskTimes(0), new TaskTimes(3600))));

		setTime(3610);
		osInterface.setIncrementTime(false);

		commands.execute(printStream, "times --list default --summary");
		
		assertOutput(
				"Times summary for list 'default'",
				"",
				"01h 00m 10s   3 - 'Test 3'",
				"    59m 50s   1 - 'Test 1'",
				"        10s   4 - 'Test 4'",
				"",
				"02h 00m 10s     - Total Time",
				""
		);
	}
	
	@Test
	void times_summary_alone_is_invalid() {
		commands.execute(printStream, "times --summary");
		
		assertOutput(
				"Invalid command.",
				""
		);
	}
}
