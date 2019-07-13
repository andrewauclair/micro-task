package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.Collections;

class Commands_Times_Summary_Test extends CommandsBaseTestCase {
	@Test
	void times_summary() {
		tasks.addTask(new Task(1, "Test 1", Task.TaskState.Inactive, Collections.singletonList(new TaskTimes(1234, 1694))));
		tasks.addTask(new Task(2, "Test 2"));
		tasks.addTask(new Task(30, "Test 3", Task.TaskState.Inactive, Collections.singletonList(new TaskTimes(90))));

		setTime(4000);

		commands.execute("times --list default --summary");

		assertOutput(
				"Times summary for list 'default'",
				"",
				"01h 05m 10s   30 - 'Test 3'",
				"    07m 40s   1 - 'Test 1'",
				"        00s   2 - 'Test 2'",
				"",
				"01h 12m 50s     - Total Time",
				""
		);
	}
}
