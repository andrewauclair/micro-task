// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskContainerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Finish_List_Test extends CommandsBaseTestCase {
	@Test
	void finish_a_list() {
		tasks.addList("/test", true);

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"Finished list '/test'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getListByName("/test").getState());
	}

	@Test
	void not_allowed_to_finish_active_list() {
		tasks.setActiveList("/default");

		commands.execute(printStream, "finish --list /default");

		assertOutput(
				"List to finish must not be active.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName("/default").getState());
	}

	@Test
	void lists_with_tasks_that_are_not_finished_cannot_be_finished() {
		tasks.addList("/test", true);

		tasks.setActiveList("/test");
		tasks.addTask("Test 1");

		tasks.setActiveList("/default");

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"List to finish still has tasks to complete.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName("/test").getState());
	}
}
