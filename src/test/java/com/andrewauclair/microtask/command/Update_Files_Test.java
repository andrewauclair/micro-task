// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.task.Task;
import com.andrewauclair.microtask.task.TaskGroup;
import com.andrewauclair.microtask.task.Tasks;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.andrewauclair.microtask.TestUtils.createInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class Update_Files_Test extends CommandsBaseTestCase {
	@Test
	void update_files_on_all_lists() throws IOException {
		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");
		tasks.addList(newList("one"), "overhead-general", true);
		tasks.setCurrentList(existingList("one"));
		Task task3 = tasks.addTask("Test");
		Task task4 = tasks.addTask("Test");
		Task task5 = tasks.addTask("Test");

		tasks.createGroup(newGroup("/projects/"));

		tasks.createGroup(newGroup("/test/two/three/"));
		tasks.addList(newList("/test/two/three/five"), "none", true);
		tasks.setCurrentList(existingList("/test/two/three/five"));

		Task task6 = tasks.addTask("Test");

		Mockito.reset(osInterface, writer);
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenReturn(new DataOutputStream(output));

		ByteArrayOutputStream defaultList = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/default/list.txt")).thenReturn(new DataOutputStream(defaultList));

		ByteArrayOutputStream oneList = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/one/list.txt")).thenReturn(new DataOutputStream(oneList));

		ByteArrayOutputStream fiveList = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/two/three/five/list.txt")).thenReturn(new DataOutputStream(fiveList));

		ByteArrayOutputStream testGroup = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/group.txt")).thenReturn(new DataOutputStream(testGroup));

		ByteArrayOutputStream testTwoGroup = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/two/group.txt")).thenReturn(new DataOutputStream(testTwoGroup));

		ByteArrayOutputStream testTwoThreeGroup = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/test/two/three/group.txt")).thenReturn(new DataOutputStream(testTwoThreeGroup));

		ByteArrayOutputStream projectGroup = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/tasks/projects/group.txt")).thenReturn(new DataOutputStream(projectGroup));

		UpdateCommand.updateFiles(tasks, osInterface, localSettings, projects, schedule, commands);

		InOrder order = Mockito.inOrder(writer, osInterface);

		order.verify(osInterface).createOutputStream("git-data/tasks/default/list.txt");
		order.verify(writer).writeTask(task1, "git-data/tasks/default/1.txt");
		order.verify(writer).writeTask(task2, "git-data/tasks/default/2.txt");
		order.verify(osInterface).createOutputStream("git-data/tasks/one/list.txt");
		order.verify(writer).writeTask(task3, "git-data/tasks/one/3.txt");
		order.verify(writer).writeTask(task4, "git-data/tasks/one/4.txt");
		order.verify(writer).writeTask(task5, "git-data/tasks/one/5.txt");
		order.verify(osInterface).createOutputStream("git-data/tasks/test/group.txt");
		order.verify(osInterface).createOutputStream("git-data/tasks/test/two/group.txt");
		order.verify(osInterface).createOutputStream("git-data/tasks/test/two/three/group.txt");
		order.verify(osInterface).createOutputStream("git-data/tasks/test/two/three/five/list.txt");
		order.verify(writer).writeTask(task6, "git-data/tasks/test/two/three/five/6.txt");
		order.verify(osInterface).createOutputStream("git-data/task-data-version.txt");
		order.verify(osInterface).gitCommit("Updating files to version '19.1.5'");

		assertOutput(
				"Updated all files.",
				""
		);

		assertEquals("19.1.5", output.toString());

		assertOutput(
				defaultList,

				"state InProgress",
				"time ",
				"state InProgress",
				"time "
		);

		assertOutput(
				oneList,

				"state InProgress",
				"time overhead-general"
		);

		assertOutput(
				fiveList,

				"state InProgress",
				"time none"
		);

		assertOutput(
				testGroup,

				"state InProgress",
				"time "
		);

		assertOutput(
				testTwoGroup,

				"state InProgress",
				"time "
		);

		assertOutput(
				testTwoThreeGroup,

				"state InProgress",
				"time "
		);
	}

	@Test
	void update_files_on_all_lists_for_unknown_version() throws IOException {
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenReturn(new DataOutputStream(output));

		Mockito.when(osInterface.createInputStream("git-data/projects.txt")).thenReturn(createInputStream(""));

		tasks.addGroup(newGroup("/projects/"));

		UpdateCommand.updateFiles(tasks, osInterface, localSettings, projects, schedule, commands);

		InOrder order = Mockito.inOrder(osInterface);

		order.verify(osInterface).gitCommit("Updating files to version 'Unknown'");

		assertOutput(
				"Updated all files.",
				""
		);
	}

	@Test
	void update_files_prints_exception_if_task_data_version_txt_fails() throws IOException {
		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenThrow(IOException.class);

		Mockito.when(osInterface.createInputStream("git-data/projects.txt")).thenReturn(createInputStream(""));

		tasks.addGroup(newGroup("/projects/"));

		UpdateCommand.updateFiles(tasks, osInterface, localSettings, projects, schedule, commands);

		assertOutput(
				"java.io.IOException",
				"Updated all files.",
				""
		);
	}

	@Test
	void update_files_reloads_tasks() throws IOException {
		Tasks tasks = Mockito.mock(Tasks.class);

		Mockito.when(tasks.getRootGroup()).thenReturn(new TaskGroup("/"));

		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenThrow(IOException.class);

		UpdateCommand.updateFiles(tasks, osInterface, localSettings, projects, schedule, commands);

		Mockito.verify(tasks).load(any(), any());
	}
}
