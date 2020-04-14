// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskContainerState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Commands_Finish_Group_Test extends CommandsBaseTestCase {
	@Test
	void finish_a_group() {
		tasks.addGroup("/test/");
		tasks.addList("/test/one", true);
		tasks.addTask("Test", "/test/one");
		tasks.finishTask(1);

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Finished group '/test/'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getGroup("/test/").getState());
		assertNotNull(tasks.getTask(1));
	}

	@Test
	void not_allowed_to_finish_active_group() {
		tasks.addList("/test/one", true);

		tasks.setActiveGroup("/test/");

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group to finish must not be active.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/").getState());
	}

	@Test
	void groups_with_tasks_that_are_not_finished_cannot_be_finished() {
		tasks.addList("/test/one", true);

		tasks.setActiveList(existingList("/test/one"));
		tasks.addTask("Test 1");

		tasks.setActiveList(existingList("/default"));

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group to finish still has tasks to complete.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName("/test/one").getState());
	}
}
