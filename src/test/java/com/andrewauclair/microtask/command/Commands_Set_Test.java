// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class Commands_Set_Test extends CommandsBaseTestCase {
	@Test
	@Disabled("Waiting until we add this option")
	void set_number_of_hours_in_day() {
		commands.execute(printStream, "set --hours-in-day=6");

		Mockito.verify(localSettings).setHoursInDay(6);

		assertOutput(
				"Set hours in day to 6",
				""
		);
	}

	@Test
	void set_task_due_date() {
		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.setCurrentList(existingList("/gr/gd/one"));
		tasks.addTask("Test");

		commands.execute(printStream, "set task 1 --due p1w");

		assertOutput(
				"Set due date for task 1 - 'Test' to 01/07/1970 06:33:20 PM",
				""
		);
	}

	@Test
	void set_task_due_today() {
		tasks.addTask("Test");

		osInterface.setTime(50000);

		commands.execute(printStream, "set task 1 --due-today");

		assertOutput(
				"Set due date for task 1 - 'Test' to 01/01/1970 07:53:20 AM",
				""
		);
	}

	@Test
	void set_task_command_help() {
		commands.execute(printStream, "set task --help");

		assertOutput(
				"Usage:  set task [-hr] [--due-today] [--inactive] [--not-recurring]",
						"                 [--due=<due>] [--add-tags=<addTags>[,<addTags>...]]...",
						"                 [--remove-tags=<removeTags>[,<removeTags>...]]... <id>",
						"      <id>              Task to set.",
						"      --add-tags=<addTags>[,<addTags>...]",
						"                        Tags to add to task.",
						"      --due=<due>       Due time of the task.",
						"      --due-today       Set due date of task as today.",
						"  -h, --help            Show this help message.",
						"      --inactive        Set task state to inactive.",
						"      --not-recurring   Set task to non-recurring.",
						"  -r, --recurring       Set task to recurring.",
						"      --remove-tags=<removeTags>[,<removeTags>...]",
						"                        Tags to remove from task."
		);
	}

	@Test
	void set_list_due_date__period() {
		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.setCurrentList(existingList("/gr/gd/one"));
		tasks.addTask("Test");
		tasks.addTask("Test");

		commands.execute(printStream, "set list /gr/gd/one --due p1w");

		assertOutput(
				"Set due date for task 1 - 'Test' to 01/07/1970 06:50:00 PM",
				"Set due date for task 2 - 'Test' to 01/07/1970 06:50:00 PM",
				""
		);
	}

	@Test
	void set_list_due_date__local_date() {
		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.setCurrentList(existingList("/gr/gd/one"));
		tasks.addTask("Test");
		tasks.addTask("Test");

		commands.execute(printStream, "set list /gr/gd/one --due 1971-02-15");

		assertOutput(
				"Set due date for task 1 - 'Test' to 02/15/1971 12:00:00 AM",
				"Set due date for task 2 - 'Test' to 02/15/1971 12:00:00 AM",
				""
		);
	}

	@Test
	void set_list_due_date__month_day() {
		osInterface.setTime(50000);

		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.setCurrentList(existingList("/gr/gd/one"));
		tasks.addTask("Test");
		tasks.addTask("Test");

		commands.execute(printStream, "set list /gr/gd/one --due --02-15");

		assertOutput(
				"Set due date for task 1 - 'Test' to 02/15/1970 12:00:00 AM",
				"Set due date for task 2 - 'Test' to 02/15/1970 12:00:00 AM",
				""
		);
	}

	@Test
	void set_group_due_date__period() {
		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.addList(newList("/gr/gd/two"), true);

		tasks.addTask("Test", existingList("/gr/gd/one"));
		tasks.addTask("Test", existingList("/gr/gd/one"));
		tasks.addTask("Test", existingList("/gr/gd/two"));
		tasks.addTask("Test", existingList("/gr/gd/two"));

		commands.execute(printStream, "set group /gr/gd/ --due p1w");

		assertOutput(
				"Set due date for task 1 - 'Test' to 01/07/1970 07:23:20 PM",
				"Set due date for task 2 - 'Test' to 01/07/1970 07:23:20 PM",
				"Set due date for task 3 - 'Test' to 01/07/1970 07:23:20 PM",
				"Set due date for task 4 - 'Test' to 01/07/1970 07:23:20 PM",
				""
		);
	}

	@Test
	void set_group_due_date__local_date() {
		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.addList(newList("/gr/gd/two"), true);

		tasks.addTask("Test", existingList("/gr/gd/one"));
		tasks.addTask("Test", existingList("/gr/gd/one"));
		tasks.addTask("Test", existingList("/gr/gd/two"));
		tasks.addTask("Test", existingList("/gr/gd/two"));

		commands.execute(printStream, "set group /gr/gd/ --due 1971-02-15");

		assertOutput(
				"Set due date for task 1 - 'Test' to 02/15/1971 12:00:00 AM",
				"Set due date for task 2 - 'Test' to 02/15/1971 12:00:00 AM",
				"Set due date for task 3 - 'Test' to 02/15/1971 12:00:00 AM",
				"Set due date for task 4 - 'Test' to 02/15/1971 12:00:00 AM",
				""
		);
	}

	@Test
	void set_group_due_date__month_day() {
		osInterface.setTime(50000);

		tasks.addGroup(newGroup("/gr/gd/"));
		tasks.addList(newList("/gr/gd/one"), true);
		tasks.addList(newList("/gr/gd/two"), true);

		tasks.addTask("Test", existingList("/gr/gd/one"));
		tasks.addTask("Test", existingList("/gr/gd/one"));
		tasks.addTask("Test", existingList("/gr/gd/two"));
		tasks.addTask("Test", existingList("/gr/gd/two"));

		commands.execute(printStream, "set group /gr/gd/ --due --02-15");

		assertOutput(
				"Set due date for task 1 - 'Test' to 02/15/1970 12:00:00 AM",
				"Set due date for task 2 - 'Test' to 02/15/1970 12:00:00 AM",
				"Set due date for task 3 - 'Test' to 02/15/1970 12:00:00 AM",
				"Set due date for task 4 - 'Test' to 02/15/1970 12:00:00 AM",
				""
		);
	}

	@Test
	void set_list_due_today() {
		tasks.addTask("Test");

		osInterface.setTime(50000);
		osInterface.setIncrementTime(false);

		commands.execute(printStream, "set list /default --due-today");

		assertOutput(
				"Set due date for task 1 - 'Test' to 01/01/1970 07:53:20 AM",
				""
		);
	}

	@Test
	void set_list_command_help() {
		commands.execute(printStream, "set list --help");

		assertOutput(
				"Usage:  set list [-h] ([--in-progress] [--due=<due> | --due-today]) <list>",
						"      <list>          The list to set.",
						"      --due=<due>     Set the due time for all tasks in the list.",
						"      --due-today     Set the due time for all tasks in the list to today.",
						"  -h, --help          Show this help message.",
						"      --in-progress   Set the list state to in progress."
		);
	}

	@Test
	void set_group_command_help() {
		commands.execute(printStream, "set group --help");

		assertOutput(
				"Usage:  set group [-h] ([--in-progress] [--due=<due> | --due-today]) <group>",
						"      <group>         The group to set.",
						"      --due=<due>     Set the due time for all tasks in the group.",
						"      --due-today     Set the due time for all tasks in the group to today.",
						"  -h, --help          Show this help message.",
						"      --in-progress   Set the list state to in progress."
		);
	}
}
