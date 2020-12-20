// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.TestUtils.newTaskBuilder;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_RED;

public class Commands_Tasks_Due_Test extends CommandsBaseTestCase {
	@Test
	void finished_tasks_are_not_due() {
		tasks.addTask(new TaskBuilder(tasks.nextID())
				.withTask("Test")
				.withState(TaskState.Finished)
				.withDueTime(0)
				.build());

		commands.execute(printStream, "tasks");

		assertOutput(
				"Tasks on list '/default'",
				"",
				"No tasks.",
				""
		);
	}

	@Test
	void recurring_tasks_are_not_due() {
		tasks.addTask(new TaskBuilder(tasks.nextID())
				.withTask("Test")
				.withState(TaskState.Inactive)
				.withRecurring(true)
				.withDueTime(0)
				.build());

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + " R     1  Test       " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 1 (1 Recurring)" + ANSI_RESET,
				""
		);
	}

	@Test
	void display_tasks_due_before() {
		int time_in_day = 86000;

		tasks.addTask(newTaskBuilder(1, "Test", TaskState.Inactive, 0).withDueTime(0).build());
		tasks.addTask(newTaskBuilder(2, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 2).build());
		tasks.addTask(newTaskBuilder(3, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 3).build());
		tasks.addTask(newTaskBuilder(4, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 4).build());
		tasks.addTask(newTaskBuilder(5, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 5).build());
		tasks.addTask(newTaskBuilder(6, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 6).build());
		tasks.addTask(newTaskBuilder(7, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 7).build());
		tasks.addTask(newTaskBuilder(8, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 8).build());
		tasks.addTask(newTaskBuilder(9, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 9).build());
		tasks.addTask(newTaskBuilder(10, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 10).build());
		tasks.addTask(newTaskBuilder(11, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 11).build());
		tasks.addTask(newTaskBuilder(12, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 12).build());

		commands.execute(printStream, "tasks --due-before 1970-01-07");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks due before 01/07/1970",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_RED + "       6  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       5  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       4  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       3  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       2  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       1  Test       " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 6" + ANSI_RESET,
				""
		);
	}
}
