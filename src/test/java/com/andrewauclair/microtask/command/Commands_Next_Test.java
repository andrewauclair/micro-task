// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import com.andrewauclair.microtask.task.build.TaskBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;

class Commands_Next_Test extends CommandsBaseTestCase {
	@Test
	void execute_next_command_for_5_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addGroup(newGroup("/gr/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 3");
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 5 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   1  Test 1     " + ANSI_RESET,
				"/default   2  Test 2     ",
				ANSI_BG_GRAY + "/gr/one    3  Test 3     " + ANSI_RESET,
				"/gr/one    4  Test 4     ",
				ANSI_BG_GRAY + "/gr/one    5  Test 5     " + ANSI_RESET,
				""
		);
	}

	@Test
	void execute_next_command_for_2_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		commands.execute(printStream, "next --count 2");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   1  Test 1     " + ANSI_RESET,
				"/default   2  Test 2     ",
				""
		);
	}

	@Test
	void next_command_skips_finished_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		tasks.finishTask(existingID(2));
		tasks.startTask(existingID(1), false);

		commands.execute(printStream, "next -c 2");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GREEN + "/default   1  Test 1     " + ANSI_RESET,
				"/default   3  Test 3     ",
				""
		);
	}

	@Test
	void next_command_skips_recurring_tasks() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		tasks.setRecurring(existingID(2), true);

		commands.execute(printStream, "next -c 2");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   1  Test 1     " + ANSI_RESET,
				"/default   3  Test 3     ",
				""
		);
	}

	@Test
	void next_command_prints_all_available_if_less_than_required() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		commands.execute(printStream, "next -c 5");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   1  Test 1     " + ANSI_RESET,
				"/default   2  Test 2     ",
				""
		);
	}

	@Test
	void next_command_cuts_off_names_when_not_verbose() {
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(80);

		commands.execute(printStream, "next -c 5");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r + "                              ",
				ANSI_BG_GRAY + "/default   1  Very long titles will be wrapped at the side of the screen ins..." + ANSI_RESET,
				"/default   2  Very long titles will be wrapped at the side of the screen ins...",
				""
		);
	}

	@Test
	void next_command_does_not_cut_off_names_when_verbose() {
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(80);

		commands.execute(printStream, "next -c 5 -v");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r + "                              ",
				ANSI_BG_GRAY + "/default   1  Very long titles will be wrapped at the side of the screen       ",
				"              instead of being cut off at the edge                             " + ANSI_RESET,
				"/default   2  Very long titles will be wrapped at the side of the screen       ",
				"              instead of being cut off at the edge                             ",
				""
		);
	}

	@Test
	void ignore_a_list_on_the_next_command() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addGroup(newGroup("/gr/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 4");
		tasks.addList(newList("/gr/two"), true);
		tasks.setCurrentList(existingList("/gr/two"));
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5 --exclude-list /gr/one");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 4 Tasks to Complete (exclude: /gr/one)",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   1  Test 1     " + ANSI_RESET,
				"/default   2  Test 2     ",
				ANSI_BG_GRAY + "/default   3  Test 3     " + ANSI_RESET,
				"/gr/two    5  Test 5     ",
				""
		);
	}

	@Test
	void ignore_a_group_on_the_next_command() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addGroup(newGroup("/gr/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 4");
		tasks.addList(newList("/gr/two"), true);
		tasks.setCurrentList(existingList("/gr/two"));
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5 --exclude-group /gr/");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 3 Tasks to Complete (exclude: /gr/)",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   1  Test 1     " + ANSI_RESET,
				"/default   2  Test 2     ",
				ANSI_BG_GRAY + "/default   3  Test 3     " + ANSI_RESET,
				""
		);
	}

	@Test
	void exclusive_to_single_list_on_the_next_command() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addGroup(newGroup("/gr/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 4");
		tasks.addList(newList("/gr/two"), true);
		tasks.setCurrentList(existingList("/gr/two"));
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5 --list /gr/one");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 1 Tasks to Complete (include: /gr/one)",
				"",
				u + "List" + r + "     " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/gr/one   4  Test 4     " + ANSI_RESET,
				""
		);
	}

	@Test
	void inclusive_to_a_single_group_on_the_next_command() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addGroup(newGroup("/gr/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 4");
		tasks.addList(newList("/gr/two"), true);
		tasks.setCurrentList(existingList("/gr/two"));
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5 --group /gr/");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete (include: /gr/)",
				"",
				u + "List" + r + "     " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/gr/one   4  Test 4     " + ANSI_RESET,
				"/gr/two   5  Test 5     ",
				""
		);
	}

	@Test
	void inclusive_to_a_single_group__and_excluding_list_on_the_next_command() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");
		tasks.addGroup(newGroup("/gr/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 4");
		tasks.addList(newList("/gr/two"), true);
		tasks.setCurrentList(existingList("/gr/two"));
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5 --group /gr/ --exclude-list /gr/one");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 1 Tasks to Complete (include: /gr/; exclude: /gr/one)",
				"",
				u + "List" + r + "     " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/gr/two   5  Test 5     " + ANSI_RESET,
				""
		);
	}

	@Test
	void inclusive_to_a_single_group__and_excluding_nested_group_on_the_next_command() {
		tasks.addTask("Test 1");
		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/one"), true);
		tasks.addList(newList("/gr/gd/two"), true);

		tasks.setCurrentList(existingList("/gr/one"));
		tasks.addTask("Test 2");
		tasks.addTask("Test 3");

		tasks.setCurrentList(existingList("/gr/gd/two"));
		tasks.addTask("Test 4");
		tasks.addTask("Test 5");

		commands.execute(printStream, "next -c 5 --group /gr/ --exclude-group /gr/gd/");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 2 Tasks to Complete (include: /gr/; exclude: /gr/gd/)",
				"",
				u + "List" + r + "     " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "/gr/one   2  Test 2     " + ANSI_RESET,
				"/gr/one   3  Test 3     ",
				""
		);
	}

	@Test
	void cannot_use_list_and_exclude_list_at_same_time() {
		commands.execute(printStream, "next -c 5 --list /default --exclude-list /default");

		assertOutput(
				"--list and --exclude-list cannot be used together.",
				""
		);
	}

	@Test
	void next_command_displays_due_tasks_with_due_option() {
		tasks.addTask(new TaskBuilder(1)
				.withTask("Test 1")
				.withDueTime(100000)
				.build());

		tasks.addTask(new TaskBuilder(2)
				.withTask("Test 2")
				.withDueTime(90000)
				.build());

		tasks.addTask(new TaskBuilder(3)
				.withTask("Test 3")
				.withDueTime(80000)
				.build());

		tasks.addTask(new TaskBuilder(4)
				.withTask("Test 4")
				.withDueTime(70000)
				.build());

		tasks.addTask(new TaskBuilder(5)
				.withTask("Test 5")
				.withDueTime(60000)
				.build());

		commands.execute(printStream, "next -c 3 --due");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Next 3 Due Tasks to Complete",
				"",
				u + "List" + r + "      " + u + "ID" + r + "  " + u + "Due" + r + "         " + u + "Description" + r,
				ANSI_BG_GRAY + "/default   5  01/01/1970  Test 5     " + ANSI_RESET,
				"/default   4  01/01/1970  Test 4     ",
				ANSI_BG_GRAY + "/default   3  01/01/1970  Test 3     " + ANSI_RESET,
				""
		);
	}

	@Test
	void next_command_help() {
		commands.execute(printStream, "next --help");

		assertOutput(
				"Usage:  next [-hv] [--due] [-c=<count>] [--exclude-group=<excludeGroup>]...",
				"             [--exclude-list=<excludeList>]... [--group=<group>]...",
				"             [--list=<list>]...",
				"Display the next tasks to be completed.",
				"  -c, --count=<count>   Number of tasks to display.",
				"      --due             Show tasks that are due.",
				"      --exclude-group=<excludeGroup>",
				"                        Group to exclude from the next search.",
				"      --exclude-list=<excludeList>",
				"                        List to exclude from the next search.",
				"      --group=<group>   Group to include in the next search.",
				"  -h, --help            Show this help message.",
				"      --list=<list>     List to include in the next search.",
				"  -v, --verbose         Display the full description of a task."
		);
	}
}
