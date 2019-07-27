// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		
		commands.execute(printStream, "search \"monday\"");
		
		assertEquals("Search Results (4):" + Utils.NL + Utils.NL +
						"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'" + Utils.NL +
						"3 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'" + Utils.NL +
						"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'" + Utils.NL +
						"7 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'" + Utils.NL + Utils.NL
				, outputStream.toString());
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

		commands.execute(printStream, "search \"monday\"");

		assertEquals("Search Results (2):" + Utils.NL + Utils.NL +
						"3 - 'finish this task by \u001B[1m\u001B[7mmonday\u001B[0m'" + Utils.NL +
						"7 - 'some days are long, \u001B[1m\u001B[7mmonday\u001B[0ms are the longest days'" + Utils.NL + Utils.NL
				, outputStream.toString());
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

		commands.execute(printStream, "search \"monday\" " + finished);

		assertEquals("Search Results (2):" + Utils.NL + Utils.NL +
						"1 - 'do this task on \u001B[1m\u001B[7mmonday\u001B[0m'" + Utils.NL +
						"5 - '\u001B[1m\u001B[7mmonday\u001B[0m is a holiday, don't forget'" + Utils.NL + Utils.NL
				, outputStream.toString());
	}
}
