// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.TaskState;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

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
}
