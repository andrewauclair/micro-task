// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

class Commands_Times_Summary_Test extends CommandsBaseTestCase {
	@Test
	void times_summary() {
		tasks.addTask(new Task(1, "Test 1", TaskState.Inactive, Arrays.asList(new TaskTimes(0), new TaskTimes(0, 3590))));
		tasks.addTask(new Task(2, "Test 2", TaskState.Inactive, Collections.singletonList(new TaskTimes(0))));
		tasks.addTask(new Task(30, "Test 3", TaskState.Inactive, Arrays.asList(new TaskTimes(0), new TaskTimes(0))));
		
		setTime(3610);
		osInterface.setIncrementTime(false);

		commands.execute(printStream, "times --list default --summary");
		
		assertOutput(
				"Times summary for list 'default'",
				"",
				"01h 00m 10s   30 - 'Test 3'",
				"    59m 50s   1 - 'Test 1'",
				"",
				"02h 00m 00s     - Total Time",
				""
		);
	}
}
