// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import java.util.Locale;

// Test for a simple times command to execute out a task times list, might just be a temporary step towards bigger better features
class Commands_Times_Task_Test extends CommandsBaseTestCase {
	@Test
	void times_command_prints_all_task_times() {
		tasks.addTask("Test 1");

		addTaskWithTimes("Test 2", 1561078202, 1561079202);

		addTaskTimes(2, 1561080202, 1561081202);

		addTaskTimes(2, 1561082202, 1561083202);

		setTime(1561084202);
		tasks.startTask(2, false);

		setTime(1561085202);

		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --task 2");

		assertOutput(
				"Times for task 2 - 'Test 2'",
				"",
				"06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM",
				"06/20/2019 08:23:22 PM - 06/20/2019 08:40:02 PM",
				"06/20/2019 08:56:42 PM - 06/20/2019 09:13:22 PM",
				"06/20/2019 09:30:02 PM -",
				"",
				"Total time: 01h 06m 40s",
				""
		);
	}

	@Test
	void prints_times_for_task_on_different_list() {
		addTaskWithTimes("Test 1", 1561078202, 1561079202);

		tasks.addList("test");
		tasks.setCurrentList("test");

		tasks.addTask("Test 1");

		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --task 1");

		assertOutput(
				"Times for task 1 - 'Test 1'",
				"",
				"06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM",
				"",
				"Total time: 16m 40s",
				""
		);
	}
	
	@Test
	void prints_total_time_under_a_minute() {
		addTaskWithTimes("Test 1", 1561078202, 1561078260);
		
		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --task 1");
		
		assertOutput(
				"Times for task 1 - 'Test 1'",
				"",
				"06/20/2019 07:50:02 PM - 06/20/2019 07:51:00 PM",
				"",
				"Total time: 58s",
				""
		);
	}
	
	@Test
	void times_command_prints_no_task_found_if_task_does_not_exist() {
		commands.execute(printStream, "times --task 1");

		assertOutput(
				"Task not found.",
				""
		);
	}

	@Test
	void times_command_prints_no_times_for_task_when_task_has_never_been_started() {
		tasks.addTask("Test");
		
		commands.execute(printStream, "times --task 1");

		assertOutput(
				"No times for task 1 - 'Test'",
				""
		);
	}

	// TODO Print some actual usage info
	@Test
//	@Disabled("Removing until we do correct usage checking")
	void times_without_a_task_number_prints_invalid_command() {
		commands.execute(printStream, "times");

		assertOutput(
				"Invalid command.",
				""
		);
	}

	// TODO Print some actual usage info
	@Test
//	@Disabled("Removing until we do correct usage checking")
	void times_with_too_many_arguments_prints_invalid_command() {
		commands.execute(printStream, "times 1 2");

		assertOutput(
				"Invalid command.",
				""
		);
	}

	@Test
	void times_with_no_parameters_prints_the_times_of_the_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");

		setTime(1561078202);
		tasks.startTask(2, false);

		setTime(1561079202);
		tasks.stopTask();

		setTime(1561080202);
		tasks.startTask(2, false);

		setTime(1561081202);
		tasks.stopTask();

		setTime(1561082202);
		tasks.startTask(2, false);

		setTime(1561083202);
		tasks.stopTask();

		setTime(1561084202);
		tasks.startTask(2, false);

		setTime(1561085202);

		Locale.setDefault(Locale.US);
		commands.execute(printStream, "times --task 2");

		assertOutput(
				"Times for task 2 - 'Test 2'",
				"",
				"06/20/2019 07:50:02 PM - 06/20/2019 08:06:42 PM",
				"06/20/2019 08:23:22 PM - 06/20/2019 08:40:02 PM",
				"06/20/2019 08:56:42 PM - 06/20/2019 09:13:22 PM",
				"06/20/2019 09:30:02 PM -",
				"",
				"Total time: 01h 06m 40s",
				""
		);
	}
}