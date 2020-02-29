// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class Commands_Update_Tasks_Test extends CommandsBaseTestCase {
	@Test
	void update_commands_on_all_lists() throws IOException {
		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");
		tasks.addList("one", true);
		tasks.setActiveList("one");
		Task task3 = tasks.addTask("Test");
		Task task4 = tasks.addTask("Test");
		Task task5 = tasks.addTask("Test");

		tasks.addList("/test/two/three/five", true);
		tasks.setActiveList("/test/two/three/five");

		Task task6 = tasks.addTask("Test");

		Mockito.reset(osInterface, writer);
		Mockito.when(osInterface.getVersion()).thenReturn("19.1.5");

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenReturn(new DataOutputStream(output));

		commands.execute(printStream, "update --tasks");
		InOrder order = Mockito.inOrder(osInterface);

		Mockito.verify(writer).writeTask(task1, "git-data/tasks/default/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/default/2.txt");
		Mockito.verify(writer).writeTask(task3, "git-data/tasks/one/3.txt");
		Mockito.verify(writer).writeTask(task4, "git-data/tasks/one/4.txt");
		Mockito.verify(writer).writeTask(task5, "git-data/tasks/one/5.txt");
		Mockito.verify(writer).writeTask(task6, "git-data/tasks/test/two/three/five/6.txt");
		order.verify(osInterface).createOutputStream("git-data/task-data-version.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Updating task files to version '19.1.5'\"", false);

		assertOutput(
				"Updated all tasks.",
				""
		);

		assertEquals("19.1.5", output.toString());
	}

	@Test
	void update_commands_on_all_lists_for_unknown_version() throws IOException {
		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");
		tasks.addList("one", true);
		tasks.setActiveList("one");
		Task task3 = tasks.addTask("Test");
		Task task4 = tasks.addTask("Test");
		Task task5 = tasks.addTask("Test");

		Mockito.reset(osInterface, writer);
		Mockito.when(osInterface.getVersion()).thenThrow(IOException.class);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenReturn(new DataOutputStream(output));

		commands.execute(printStream, "update --tasks");
		InOrder order = Mockito.inOrder(osInterface);

		Mockito.verify(writer).writeTask(task1, "git-data/tasks/default/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/default/2.txt");
		Mockito.verify(writer).writeTask(task3, "git-data/tasks/one/3.txt");
		Mockito.verify(writer).writeTask(task4, "git-data/tasks/one/4.txt");
		Mockito.verify(writer).writeTask(task5, "git-data/tasks/one/5.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Updating task files to version 'Unknown'\"", false);

		assertOutput(
				"Updated all tasks.",
				""
		);
	}

	@Test
	void update_command_prints_exception_if_task_data_version_txt_fails() throws IOException {
		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenThrow(IOException.class);

		commands.execute(printStream, "update --tasks");

		assertOutput(
				"java.io.IOException",
				"Updated all tasks.",
				""
		);
	}

	@Test
	void update_tasks_reloads_tasks() throws IOException {
		Tasks tasks = Mockito.mock(Tasks.class);

		Mockito.when(osInterface.createOutputStream("git-data/task-data-version.txt")).thenThrow(IOException.class);

		commands = new Commands(tasks, gitLabReleases, osInterface);

		commands.execute(printStream, "update --tasks");

		Mockito.verify(tasks).load(any(), any());
	}
}
