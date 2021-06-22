// Copyright (C) 2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.project.NewProject;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static org.assertj.core.api.Assertions.assertThat;

public class Commands_Tasks_Schedule_Test extends CommandsBaseTestCase {
	@Test
	void display_scheduled_tasks_including_finished_tasks() {
		schedule_day(); // 4, 8, 3, 7, 11, 15, 2, 6, 10, 14, 18, 22, 1, 5, 9, 13

		tasks.finishTask(existingID(4));
		tasks.finishTask(existingID(7));
		tasks.finishTask(existingID(22));
		tasks.startTask(existingID(2), false);

		commands.execute(printStream, "tasks --schedule");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on Schedule",
				"",
				u + "Project" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "proj-1     F   22  Test       " + ANSI_RESET,
				"proj-1         18  Test       ",
				ANSI_BG_GRAY + "proj-2         15  Test       " + ANSI_RESET,
				"proj-1         14  Test       ",
				ANSI_BG_GRAY + "               13  Test       " + ANSI_RESET,
				"proj-2         11  Test       ",
				ANSI_BG_GRAY + "proj-1         10  Test       " + ANSI_RESET,
				"                9  Test       ",
				ANSI_BG_GRAY + "proj-3          8  Test       " + ANSI_RESET,
				"proj-2     F    7  Test       ",
				ANSI_BG_GRAY + "proj-1          6  Test       " + ANSI_RESET,
				"                5  Test       ",
				ANSI_BG_GRAY + "proj-3     F    4  Test       " + ANSI_RESET,
				"proj-2          3  Test       ",
				ANSI_BG_GREEN + "proj-1   *      2  Test       " + ANSI_RESET,
				"                1  Test       ",
				"",
				ANSI_BOLD + "Total Tasks: 16" + ANSI_RESET,
				""
		);
	}

	private void schedule_day() {
		projects.createProject(new NewProject(projects, "proj-1"), false);
		projects.createProject(new NewProject(projects, "proj-2"), false);
		projects.createProject(new NewProject(projects, "proj-3"), false);

		commands.execute(printStream, "schedule --project proj-1 --pct 35");
		commands.execute(printStream, "schedule --project proj-2 --pct 25");
		commands.execute(printStream, "schedule --project proj-3 --pct 15");
		// leaves 25% unallocated which will pull from lowest numbered tasks not in scheduled projects

		for (int i = 0; i < 15; i++) {
			tasks.addTask("Test", new ExistingListName(tasks, "/default"));
			tasks.addTask("Test", new ExistingListName(tasks, "/projects/proj-1/general"));
			tasks.addTask("Test", new ExistingListName(tasks, "/projects/proj-2/general"));
			tasks.addTask("Test", new ExistingListName(tasks, "/projects/proj-3/general"));
		}
		assertThat(schedule.tasks()).isEmpty();

		commands.execute(printStream, "schedule --day");
	}
}
