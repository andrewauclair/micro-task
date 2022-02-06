// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;

import static com.andrewauclair.microtask.TestUtils.newTask;
import static com.andrewauclair.microtask.Utils.NL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Finish_Test extends TaskBaseTestCase {
	@Test
	void finish_writes_file_on_correct_list() throws IOException {
		tasks.addTask("Test");
		tasks.addList(newList("one"), true);

		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("one"));

		tasks.addTask("Test 2");

		Mockito.reset(writer);

		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/archive.txt")).thenReturn(archiveStream);

		Task task = tasks.finishTask();

		Mockito.verify(writer).writeTask(task, archiveStream);
	}

	@Test
	void finish_multiple_files_and_write_archive() throws IOException {
		tasks.addList(newList("one"), true);

		tasks.setCurrentList(existingList("one"));

		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/one/archive.txt")).thenReturn(archiveStream);

		Task task1 = tasks.finishTask(existingID(1));

		Mockito.reset(writer);

		Task task2 = tasks.finishTask(existingID(2));

		Mockito.verify(writer).writeTask(task1, archiveStream);
		Mockito.verify(writer).writeTask(task2, archiveStream);
	}

	@Test
	void finish_tells_git_control_to_add_correct_files() throws IOException {
		tasks.addTask("Test");
		tasks.addList(newList("one"), true);

		tasks.startTask(existingID(1), false);

		tasks.setCurrentList(existingList("one"));

		tasks.addTask("Test 2");

		Mockito.reset(osInterface);

		DataOutputStream archiveStream = new DataOutputStream(new ByteArrayOutputStream());
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/archive.txt")).thenReturn(archiveStream);

		tasks.finishTask();

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Finished task 1 - 'Test'");
	}

	@Test
	void finish_task_when_on_different_list() {
		tasks.addTask("Test");

		tasks.startTask(existingID(1), false);

		tasks.addList(newList("test"), true);
		tasks.setCurrentList(existingList("test"));

		Task task = tasks.finishTask(existingID(1));

		assertEquals(
				newTask(1, "Test", TaskState.Finished, 1000, 3000, Collections.singletonList(new TaskTimes(2000, 3000))),
				task
		);
	}

	@Test
	void recurring_tasks_cannot_be_finished() {
		tasks.addTask("Test");
		tasks.setRecurring(existingID(1), true);

		Task task = tasks.startTask(existingID(1), false);

		Mockito.reset(osInterface);

		TaskException taskException = assertThrows(TaskException.class, tasks::finishTask);

		assertEquals("Recurring tasks cannot be finished.", taskException.getMessage());

		assertThat(tasks.getAllTasks()).containsOnly(task);

		Mockito.verifyNoInteractions(osInterface);
	}

	@Test
	void finished_tasks_cannot_be_finished() {
		tasks.addTask("Test");

		Task task = tasks.finishTask(existingID(1));

		Mockito.reset(osInterface);

		TaskException taskException = assertThrows(TaskException.class, () -> tasks.finishTask(existingID(1)));

		assertEquals("Task 1 has already been finished.", taskException.getMessage());

		assertThat(tasks.getAllTasks()).containsOnly(task);

		Mockito.verifyNoInteractions(osInterface);
	}

	@Test
	void finish_list_writes_file() throws IOException {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/one/list.txt")).thenReturn(new DataOutputStream(outputStream));

		tasks.finishList(existingList("/test/one"));

		assertThat(outputStream.toString()).isEqualTo(
				"state Finished" + NL +
						"time none" + NL
		);
	}

	@Test
	void finish_list_commits_files_to_git() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);

		tasks.finishList(existingList("/test/one"));

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Finished list '/test/one'");
	}

	@Test
	void finish_group_writes_file() throws IOException {
		tasks.addGroup(newGroup("/test/"));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(outputStream));

		tasks.finishGroup(existingGroup("/test/"));

		assertThat(outputStream.toString()).isEqualTo(
				"state Finished" + NL +
						"time none" + NL
		);
	}

	@Test
	void finish_group_commits_files_to_git() {
		tasks.addGroup(newGroup("/test/"));

		tasks.finishGroup(existingGroup("/test/"));

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Finished group '/test/'");
	}
}
