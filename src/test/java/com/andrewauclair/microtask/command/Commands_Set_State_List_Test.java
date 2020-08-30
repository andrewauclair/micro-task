// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskContainerState;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_State_List_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_list_to_in_progress() {
		tasks.addList(newList("/test"), true);

		tasks.finishList(existingList("/test"));

		commands.execute(printStream, "set list /test --in-progress");

		assertEquals(TaskContainerState.InProgress, tasks.getListByName(existingList("/test")).getState());

		assertOutput(
				"Set state of list '/test' to In Progress",
				""
		);
	}

	@Test
	void prints_full_path_when_using_relative_name_to_set_state_to_in_progress() {
		tasks.addList(newList("/test"), true);

		tasks.finishList(existingList("/test"));

		commands.execute(printStream, "set list test --in-progress");

		assertEquals(TaskContainerState.InProgress, tasks.getListByName(existingList("/test")).getState());

		assertOutput(
				"Set state of list '/test' to In Progress",
				""
		);
	}

	@Test
	void write_list_file_when_setting_list_to_in_progress() throws IOException {
		tasks.addList(newList("/test"), true);

		tasks.finishList(existingList("/test"));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "set list /test --in-progress");

		assertOutput(
				outputStream,

				"",
				"",
				"InProgress"
		);
	}

	@Test
	void write_git_commit_when_setting_in_progress() throws IOException {
		tasks.addList(newList("/test"), true);

		tasks.finishList(existingList("/test"));

		Mockito.reset(osInterface);

		ByteArrayOutputStream defaultStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/list.txt")).thenReturn(new DataOutputStream(defaultStream));

		commands.execute(printStream, "set list /test --in-progress");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Set state for list '/test' to InProgress");
	}

	@Test
	void list_needs_to_be_finished_before_it_can_be_set_back_to_in_progress() {
		tasks.addList(newList("/test"), true);

		Mockito.reset(osInterface);

		commands.execute(printStream, "set list /test --in-progress");

		Mockito.verifyNoInteractions(osInterface);

		assertOutput(
				"List '/test' must be finished first",
				""
		);
	}
}
