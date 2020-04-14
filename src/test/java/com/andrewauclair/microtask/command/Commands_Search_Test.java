// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;

class Commands_Search_Test extends CommandsBaseTestCase {
	@Test
	void search_for_simple_text() {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		commands.execute(printStream, "search -t \"monday\"");

		assertOutput(
				"Search Results (4):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"3 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'",
				"7 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'",
				""
		);
	}

	@Test
	void search_is_case_insensitive() {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("case insensitive task on Monday");

		commands.execute(printStream, "search -t \"monday\"");

		assertOutput(
				"Search Results (2):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"3 - 'case insensitive task on \u001B[1m\u001B[7mMonday\u001B[0m'",
				""
		);
	}

	@Test
	void search_hides_finished_tasks() {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		tasks.finishTask(1);
		tasks.finishTask(5);

		commands.execute(printStream, "search -t \"monday\"");

		assertOutput(
				"Search Results (2):",
				"",
				"3 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'",
				"7 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'",
				""
		);
	}

	@Test
	void search_shows_only_finished_tasks_when_finished_is_provided() {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		tasks.finishTask(1);
		tasks.finishTask(5);

		commands.execute(printStream, "search -t \"monday\" --finished");

		assertOutput(
				"Search Results (2):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'",
				""
		);
	}

	@Test
	void finished_can_be_before_the_text_to_search() {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		tasks.finishTask(1);
		tasks.finishTask(5);

		commands.execute(printStream, "search --finished --text \"monday\"");

		assertOutput(
				"Search Results (2):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'",
				""
		);
	}

	@Test
	void search_on_nested_list() {
		tasks.addList("/test/one", true);
		tasks.setActiveList(existingList("/test/one"));

		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		commands.execute(printStream, "search -t \"monday\"");

		assertOutput(
				"Search Results (4):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"3 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'",
				"7 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'",
				""
		);
	}

	@Test
	void search_for_tasks_in_a_group_displays_all_tasks_recursively() {
		tasks.addList("/test/one", true);
		tasks.addList("/test/two", true);
		tasks.addList("/three", true);

		tasks.setActiveList(existingList("/test/one"));
		tasks.addTask("do this task on monday");
		tasks.addTask("do this task on tuesday");

		tasks.setActiveList(existingList("/test/two"));
		tasks.addTask("monday is the worst");
		tasks.addTask("tuesday's gone with the wind");

		tasks.setActiveList(existingList("/three"));
		tasks.addTask("mondays are the longest days");
		tasks.addTask("wednesday isn't that great either");

		tasks.setActiveGroup("/");

		commands.execute(printStream, "search -t \"monday\" --group");

		assertOutput(
				"Search Results (3):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"3 - '\u001B[1m\u001B[7mmonday\u001B[0m is the worst'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'",
				""
		);
	}

	@Test
	void search_uses_the_active_list_when_active_group_is_different() {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		tasks.createGroup("/test/");
		tasks.setActiveGroup("/test/");

		commands.execute(printStream, "search -t \"monday\"");

		assertOutput(
				"Search Results (4):",
				"",
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"3 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'",
				"7 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'",
				""
		);
	}

	@Test
	void verbose_search_displays_what_list_each_task_is_on() {
		tasks.addList("/oranges", true);
		tasks.addList("/apples", true);

		tasks.setActiveList(existingList("/oranges"));
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");

		tasks.setActiveList(existingList("/apples"));
		tasks.addTask("some days are long, mondays are the longest days");

		tasks.setActiveList(existingList("/oranges"));

		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");

		tasks.setActiveList(existingList("/apples"));

		tasks.addTask("The Beatles?");
		tasks.addTask("finish this task by monday");

		commands.execute(printStream, "search -vgt \"monday\"");

		assertOutput(
				"Search Results (4):",
				"",
				ANSI_BOLD + "/apples" + ANSI_RESET,
				"3 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'",
				"7 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'",
				"",
				ANSI_BOLD + "/oranges" + ANSI_RESET,
				"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'",
				"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'",
				""
		);
	}

	@Test
	void search_command_help() {
		commands.execute(printStream, "search --help");

		assertOutput(
				"Usage:  search [-fghv] [-t=<text>]",
				"Search for tasks.",
				"  -f, --finished      Search for finished tasks.",
				"  -g, --group         Search for tasks in the current group recursively.",
				"  -h, --help          Show this help message.",
				"  -t, --text=<text>   The text to search for in the task names.",
				"  -v, --verbose       Display list names in results."
		);
	}
}
