// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskContainerState;
import com.andrewauclair.microtask.task.TaskGroup;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Commands_Finish_Group_Test extends CommandsBaseTestCase {
	@Test
	void finish_a_group() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addTask("Test", existingList("/test/one"));
		tasks.finishTask(existingID(1));

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Finished group '/test/'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getGroup("/test/").getState());
		assertNotNull(tasks.getTask(existingID(1)));
	}

	@Test
	void not_allowed_to_finish_active_group() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		tasks.setCurrentGroup(existingGroup("/test/"));

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group to finish must not be active.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/").getState());
	}

	@Test
	void groups_with_tasks_that_are_not_finished_cannot_be_finished() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		tasks.setCurrentList(existingList("/test/one"));
		tasks.addTask("Test 1");

		tasks.setCurrentList(existingList("/default"));

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group to finish still has tasks to complete.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName(existingList("/test/one")).getState());
	}

	@Test
	void finished_groups_cannot_be_finished() {
		tasks.addGroup(newGroup("/test/"));
		TaskGroup group = tasks.finishGroup(existingGroup("/test/"));

		Mockito.reset(osInterface, writer);

		commands.execute(printStream, "finish --group /test/");

		assertOutput(
				"Group has already been finished.",
				""
		);

		assertThat(tasks.getGroup(existingGroup("/")).getChildren()).containsOnly(
				group,
				tasks.getListByName(existingList("/default"))
		);

		Mockito.verifyNoInteractions(osInterface, writer);
	}
}
