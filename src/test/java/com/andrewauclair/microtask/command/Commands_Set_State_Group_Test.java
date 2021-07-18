// Copyright (C) 2019-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.TaskContainerState;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Set_State_Group_Test extends CommandsBaseTestCase {
	@Test
	void execute_set_group_to_in_progress() {
		tasks.addGroup(newGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/"));

		commands.execute(printStream, "set group /test/ --in-progress");

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/").getState());

		assertOutput(
				"Set state of group '/test/' to In Progress",
				""
		);
	}

	@Test
	void setting_group_to_in_progress_sets_parent_to_in_progress_when_finished() {
		tasks.addGroup(newGroup("/test/one/"));

		tasks.finishGroup(existingGroup("/test/"));

		commands.execute(printStream, "set group /test/one/ --in-progress");

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/").getState());
		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/one/").getState());

		assertOutput(
				"Set state of group '/test/one/' to In Progress",
				"Set state of group '/test/' to In Progress",
				""
		);
	}

	@Test
	void prints_full_path_when_using_relative_name_to_set_state_to_in_progress() {
		tasks.addGroup(newGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/"));

		commands.execute(printStream, "set group test/ --in-progress");

		assertEquals(TaskContainerState.InProgress, tasks.getGroup("/test/").getState());

		assertOutput(
				"Set state of group '/test/' to In Progress",
				""
		);
	}

	@Test
	void write_group_file_when_setting_group_to_in_progress() throws IOException {
		tasks.addGroup(newGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/"));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "set group /test/ --in-progress");

		assertOutput(
				outputStream,

				"InProgress"
		);
	}

	@Test
	void write_git_commit_when_setting_in_progress() throws IOException {
		tasks.addGroup(newGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/"));

		Mockito.reset(osInterface);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(outputStream));

		commands.execute(printStream, "set group /test/ --in-progress");

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Set state for group '/test/' to InProgress");
	}

	@Test
	void group_needs_to_be_finished_before_it_can_be_set_back_to_in_progress() {
		tasks.addGroup(newGroup("/test/"));

		Mockito.reset(osInterface);

		commands.execute(printStream, "set group /test/ --in-progress");

		Mockito.verifyNoInteractions(osInterface);

		assertOutput(
				"Group '/test/' must be finished first",
				""
		);
	}
}
