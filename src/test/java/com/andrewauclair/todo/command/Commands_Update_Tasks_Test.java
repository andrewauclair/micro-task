// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.task.Task;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class Commands_Update_Tasks_Test extends CommandsBaseTestCase {
	@Test
	void update_commands_on_all_lists() {
		Task task1 = tasks.addTask("Test");
		Task task2 = tasks.addTask("Test");
		tasks.addList("one");
		tasks.setActiveList("one");
		Task task3 = tasks.addTask("Test");
		Task task4 = tasks.addTask("Test");
		Task task5 = tasks.addTask("Test");

		Mockito.reset(osInterface, writer);

		commands.execute(printStream, "update --tasks");
		InOrder order = Mockito.inOrder(osInterface);

		Mockito.verify(writer).writeTask(task1, "git-data/tasks/default/1.txt");
		Mockito.verify(writer).writeTask(task2, "git-data/tasks/default/2.txt");
		Mockito.verify(writer).writeTask(task3, "git-data/tasks/one/3.txt");
		Mockito.verify(writer).writeTask(task4, "git-data/tasks/one/4.txt");
		Mockito.verify(writer).writeTask(task5, "git-data/tasks/one/5.txt");
		order.verify(osInterface).runGitCommand("git add .", false);
		order.verify(osInterface).runGitCommand("git commit -m \"Updating task files.\"", false);

		assertOutput(
				"Updated all tasks.",
				""
		);
	}
}
