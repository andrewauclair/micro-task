// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.git.GitCommand;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Tasks_Add_Test extends TaskBaseTestCase {
	@Test
	void adding_task_adds_it_to_a_list() {
		Task actualTask = tasks.addTask("Testing task add command");

		Task expectedTask = new Task(1, "Testing task add command");

		assertThat(tasks.getTasks()).containsOnly(expectedTask);
		assertEquals(expectedTask, actualTask);
	}

	@Test
	void adding_task_tells_task_writer_to_write_file() {
		Task task1 = tasks.addTask("Testing task add command 1");
		Task task2 = tasks.addTask("Testing task add command 2");

		Mockito.verify(writer).writeTask(task1, "git-data/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/2.txt");
	}

	@Test
	void adding_task_tells_git_control_to_add_file_and_commit() {
		InOrder order = Mockito.inOrder(osInterface);

		tasks.addTask("Testing task add command 1");

		order.verify(osInterface).runGitCommand(new GitCommand("git add 1.txt"));
		order.verify(osInterface).runGitCommand(new GitCommand("git commit -m \"Added task 1 - \\\"Testing task add command 1\\\"\""));

		tasks.addTask("Testing task add command 2");

		order.verify(osInterface).runGitCommand(new GitCommand("git add 2.txt"));
		order.verify(osInterface).runGitCommand(new GitCommand("git commit -m \"Added task 2 - \\\"Testing task add command 2\\\"\""));
	}

	@Test
	void tasks_allows_us_to_add_an_actual_task_object_for_reloading_from_a_file() {
		Task task = new Task(4, "Testing");
		tasks.addTask(task);

		assertThat(tasks.getTasks()).containsOnly(task);
	}
}
