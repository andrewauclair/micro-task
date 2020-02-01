// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	@ParameterizedTest
	@ValueSource(strings = {"--finished", "-f"})
	void search_shows_only_finished_tasks_when_finished_is_provided(String finished) {
		tasks.addTask("do this task on monday");
		tasks.addTask("tuesdays are ignored");
		tasks.addTask("finish this task by monday");
		tasks.addTask("wednesdays too");
		tasks.addTask("monday is a holiday, don't forget");
		tasks.addTask("The Beatles?");
		tasks.addTask("some days are long, mondays are the longest days");

		tasks.finishTask(1);
		tasks.finishTask(5);
		
		commands.execute(printStream, "search -t \"monday\" " + finished);

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

		commands.execute(printStream, "chlist /test/one");

		outputStream.reset();

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
		
		tasks.setActiveList("/test/one");
		tasks.addTask("do this task on monday");
		tasks.addTask("do this task on tuesday");
		
		tasks.setActiveList("/test/two");
		tasks.addTask("monday is the worst");
		tasks.addTask("tuesday's gone with the wind");
		
		tasks.setActiveList("/three");
		tasks.addTask("mondays are the longest days");
		tasks.addTask("wednesday isn't that great either");
		
		tasks.switchGroup("/");
		
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
		tasks.switchGroup("/test/");
		
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
}
