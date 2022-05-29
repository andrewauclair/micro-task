// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
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
		tasks.addTask(new TaskBuilder(idValidator, newID(tasks.nextID()))
				.withTask("Test")
				.withState(TaskState.Finished)
				.withDueTime(0));

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
		tasks.addTask(new TaskBuilder(idValidator, newID(tasks.nextID()))
				.withTask("Test")
				.withState(TaskState.Inactive)
				.withRecurring(true)
				.withDueTime(0));

		commands.execute(printStream, "tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_GRAY + " R     1  (1)  Test       " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 1 (1 Recurring)" + ANSI_RESET,
				""
		);
	}

	@Test
	void display_tasks_due_before() {
		osInterface.setTime(21600);
		osInterface.setIncrementTime(false);

		int first_midnight = 21600;
		int time_in_day = 86400;

		tasks.addTask(newTaskBuilder(newID(1), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight));
		tasks.addTask(newTaskBuilder(newID(2), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day));
		tasks.addTask(newTaskBuilder(newID(3), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 2));
		tasks.addTask(newTaskBuilder(newID(4), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 3));
		tasks.addTask(newTaskBuilder(newID(5), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 4));
		tasks.addTask(newTaskBuilder(newID(6), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 5));
		tasks.addTask(newTaskBuilder(newID(7), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 6));
		tasks.addTask(newTaskBuilder(newID(8), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 7));
		tasks.addTask(newTaskBuilder(newID(9), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 8));
		tasks.addTask(newTaskBuilder(newID(10), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 9));
		tasks.addTask(newTaskBuilder(newID(11), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 10));
		tasks.addTask(newTaskBuilder(newID(12), idValidator, "Test", TaskState.Inactive, 0).withDueTime(first_midnight + time_in_day * 11));

		commands.execute(printStream, "tasks --due-before 1970-01-07");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks due before 01/07/1970",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_RED + "       6  (6)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       5  (5)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       4  (4)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       3  (3)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       2  (2)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       1  (1)  Test       " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 6" + ANSI_RESET,
				""
		);
	}

	@Test
	void display_tasks_due_within() {
		int time_in_day = 86000;

		tasks.addTask(newTaskBuilder(newID(1), idValidator, "Test", TaskState.Inactive, 0).withDueTime(0));
		tasks.addTask(newTaskBuilder(newID(2), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day));
		tasks.addTask(newTaskBuilder(newID(3), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 2));
		tasks.addTask(newTaskBuilder(newID(4), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 3));
		tasks.addTask(newTaskBuilder(newID(5), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 4));
		tasks.addTask(newTaskBuilder(newID(6), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 5));
		tasks.addTask(newTaskBuilder(newID(7), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 6));
		tasks.addTask(newTaskBuilder(newID(8), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 7));
		tasks.addTask(newTaskBuilder(newID(9), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 8));
		tasks.addTask(newTaskBuilder(newID(10), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 9));
		tasks.addTask(newTaskBuilder(newID(11), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 10));
		tasks.addTask(newTaskBuilder(newID(12), idValidator, "Test", TaskState.Inactive, 0).withDueTime(time_in_day * 11));

		commands.execute(printStream, "tasks --due-within p1w");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks due within 1 week(s) ",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + r + "     " + u + "Description" + r,
				ANSI_BG_RED + "       7  (7)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       6  (6)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       5  (5)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       4  (4)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       3  (3)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       2  (2)  Test       " + ANSI_RESET,
				ANSI_BG_RED + "       1  (1)  Test       " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 7" + ANSI_RESET,
				""
		);
	}
}
