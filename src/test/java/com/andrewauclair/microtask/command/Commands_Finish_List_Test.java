// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskContainerState;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.TaskList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Finish_List_Test extends CommandsBaseTestCase {
	@Test
	void finish_a_list() {
		tasks.addList(newList("/test"), true);

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"Finished list '/test'",
				""
		);

		assertEquals(TaskContainerState.Finished, tasks.getListByName(existingList("/test")).getState());
	}

	@Test
	void not_allowed_to_finish_active_list() {
		tasks.setActiveList(existingList("/default"));

		commands.execute(printStream, "finish --list /default");

		assertOutput(
				"List to finish must not be active.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName(existingList("/default")).getState());
	}

	@Test
	void lists_with_tasks_that_are_not_finished_cannot_be_finished() {
		tasks.addList(newList("/test"), true);

		tasks.setActiveList(existingList("/test"));
		tasks.addTask("Test 1");

		tasks.setActiveList(existingList("/default"));

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"List to finish still has tasks to complete.",
				""
		);

		assertEquals(TaskContainerState.InProgress, tasks.getListByName(existingList("/test")).getState());
	}

	@Test
	void finished_lists_cannot_be_finished() {
		tasks.addList(newList("/test"), true);
		TaskList list = tasks.finishList(existingList("/test"));

		Mockito.reset(osInterface, writer);

		commands.execute(printStream, "finish --list /test");

		assertOutput(
				"List has already been finished.",
				""
		);

		assertThat(tasks.getGroup(existingGroup("/")).getChildren()).containsOnly(
				list,
				tasks.getListByName(existingList("/default"))
		);

		Mockito.verifyNoInteractions(osInterface, writer);
	}
}
