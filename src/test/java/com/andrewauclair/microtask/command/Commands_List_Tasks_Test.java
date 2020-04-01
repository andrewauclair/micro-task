// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.stream.IntStream;

import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_BOLD;
import static com.andrewauclair.microtask.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_List_Tasks_Test extends CommandsBaseTestCase {
	@Test
	void execute_list_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2, false);
		tasks.finishTask();
		tasks.startTask(3, false);
		
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"Tasks on list '/default'",
				"",
				"  1 - 'Task 1'",
				"* " + ANSI_FG_GREEN + "3 - 'Task 3'" + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 2" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void display_tasks_on_a_different_list() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2, false);
		tasks.finishTask();
		tasks.startTask(3, false);
		
		tasks.addList("test", true);
		tasks.setActiveList("test");
		
		commands.execute(printStream, "list --tasks --list default");
		
		assertOutput(
				"Tasks on list '/default'",
				"",
				"  1 - 'Task 1'",
				"* " + ANSI_FG_GREEN + "3 - 'Task 3'" + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 2" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void display_finished_tasks() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		
		tasks.finishTask(1);
		tasks.finishTask(3);
		
		commands.execute(printStream, "list --tasks --finished");
		
		assertOutput(
				"Finished tasks on list '/default'",
				"",
				"  1 - 'Task 1'",
				"  3 - 'Task 3'",
				"",
				ANSI_BOLD + "Total Finished Tasks: 2" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void display_finished_tasks_in_group() {
		tasks.createGroup("/test/");
		tasks.addList("/test/one", true);
		tasks.addList("/test/three", true);
		tasks.addTask("Test 1", "/test/one");
		tasks.addTask("Test 2", "/test/one");
		tasks.addTask("Test 3", "/test/one");
		
		tasks.addTask("Test 4", "/test/three");
		tasks.addTask("Test 5", "/test/three");
		tasks.addTask("Test 6", "/test/three");
		
		tasks.finishTask(2);
		tasks.finishTask(5);
		
		tasks.switchGroup("/test/");
		
		commands.execute(printStream, "list --tasks --group --finished");
		
		assertOutput(
				ANSI_BOLD + "/test/one" + ANSI_RESET,
				"  2 - 'Test 2'",
				"",
				ANSI_BOLD + "/test/three" + ANSI_RESET,
				"  5 - 'Test 5'",
				"",
				"",
				ANSI_BOLD + "Total Finished Tasks: 2" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_command_caps_at_20_tasks_and_displays_a_count_of_how_many_are_left() {
		IntStream.range(1, 40)
				.forEach(num -> tasks.addTask("Test " + num));
		
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"Tasks on list '/default'",
				"",
				"   1 - 'Test 1'",
				"   2 - 'Test 2'",
				"   3 - 'Test 3'",
				"   4 - 'Test 4'",
				"   5 - 'Test 5'",
				"   6 - 'Test 6'",
				"   7 - 'Test 7'",
				"   8 - 'Test 8'",
				"   9 - 'Test 9'",
				"  10 - 'Test 10'",
				"  11 - 'Test 11'",
				"  12 - 'Test 12'",
				"  13 - 'Test 13'",
				"  14 - 'Test 14'",
				"  15 - 'Test 15'",
				"  16 - 'Test 16'",
				"  17 - 'Test 17'",
				"  18 - 'Test 18'",
				"  19 - 'Test 19'",
				"  20 - 'Test 20'",
				"(19 more tasks.)",
				"",
				ANSI_BOLD + "Total Tasks: 39" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_command_shows_all_tasks_when_all_parameter_is_passed() {
		IntStream.range(1, 23)
				.forEach(num -> tasks.addTask("Test " + num));
		
		tasks.startTask(2, false);
		
		commands.execute(printStream, "list --tasks --all");
		
		assertOutput(
				"Tasks on list '/default'",
				"",
				"   1 - 'Test 1'",
				"*  " + ANSI_FG_GREEN + "2 - 'Test 2'" + ANSI_RESET,
				"   3 - 'Test 3'",
				"   4 - 'Test 4'",
				"   5 - 'Test 5'",
				"   6 - 'Test 6'",
				"   7 - 'Test 7'",
				"   8 - 'Test 8'",
				"   9 - 'Test 9'",
				"  10 - 'Test 10'",
				"  11 - 'Test 11'",
				"  12 - 'Test 12'",
				"  13 - 'Test 13'",
				"  14 - 'Test 14'",
				"  15 - 'Test 15'",
				"  16 - 'Test 16'",
				"  17 - 'Test 17'",
				"  18 - 'Test 18'",
				"  19 - 'Test 19'",
				"  20 - 'Test 20'",
				"  21 - 'Test 21'",
				"  22 - 'Test 22'",
				"",
				ANSI_BOLD + "Total Tasks: 22" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_tasks_on_a_nested_list() {
		tasks.addList("/test/one", true);
		tasks.switchGroup("/test/");
		tasks.setActiveList("/test/one");
		
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2, false);
		tasks.finishTask();
		tasks.startTask(3, false);
		
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"Tasks on list '/test/one'",
				"",
				"  1 - 'Task 1'",
				"* " + ANSI_FG_GREEN + "3 - 'Task 3'" + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 2" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_command_does_not_throw_the_no_active_task_exception() {
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"Tasks on list '/default'",
				"",
				"No tasks.",
				""
		);
	}
	
	@Test
	void list_all_tasks_in_the_active_group() {
		tasks.createGroup("/test/");
		tasks.createGroup("/test/junk");
		tasks.switchGroup("/test/");
		
		tasks.addList("/test/default", true);
		tasks.setActiveList("/test/default");
		
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2, false);
		tasks.finishTask();
		tasks.startTask(3, false);
		
		tasks.addList("one", true);
		tasks.addList("two", true);
		
		tasks.setActiveList("one");
		tasks.addTask("Task 4");
		tasks.addTask("Task 5");
		tasks.addTask("Task 6");
		
		tasks.setActiveList("two");
		tasks.addTask("Task 7");
		tasks.addTask("Task 8");
		tasks.addTask("Task 9");
		
		// add extra tasks that shouldn't be shown
		tasks.setActiveList("/default");
		tasks.addTask("Hidden 1");
		tasks.addTask("Hidden 2");
		
		tasks.addList("/hide", true);
		tasks.setActiveList("/hide");
		
		tasks.addTask("Hidden 3");
		
//		outputStream.reset();
		
		commands.execute(printStream, "list --tasks --group");
		
		assertOutput(
				ANSI_BOLD + "/test/default" + ANSI_RESET,
				"  1 - 'Task 1'",
				"* " + ANSI_FG_GREEN + "3 - 'Task 3'" + ANSI_RESET,
				"",
				ANSI_BOLD + "/test/one" + ANSI_RESET,
				"  4 - 'Task 4'",
				"  5 - 'Task 5'",
				"  6 - 'Task 6'",
				"",
				ANSI_BOLD + "/test/two" + ANSI_RESET,
				"  7 - 'Task 7'",
				"  8 - 'Task 8'",
				"  9 - 'Task 9'",
				"",
				"",
				ANSI_BOLD + "Total Tasks: 8" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void print_tasks_in_group_recursively() {
		tasks.addList("/one/two/three", true);
		tasks.addList("/one/four", true);
		tasks.setActiveList("/one/two/three");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		tasks.setActiveList("/one/four");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		tasks.addList("/one/two/five/nine", true);
		tasks.setActiveList("/one/two/five/nine");
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		tasks.switchGroup("/one/");
		
		commands.execute(printStream, "list --tasks --group --recursive");
		
		assertOutput(
				ANSI_BOLD + "/one/two/three" + ANSI_RESET,
				"  1 - 'Test'",
				"  2 - 'Test'",
				"  3 - 'Test'",
				"",
				ANSI_BOLD + "/one/two/five/nine" + ANSI_RESET,
				"  7 - 'Test'",
				"  8 - 'Test'",
				"  9 - 'Test'",
				"",
				ANSI_BOLD + "/one/four" + ANSI_RESET,
				"  4 - 'Test'",
				"  5 - 'Test'",
				"  6 - 'Test'",
				"",
				"",
				ANSI_BOLD + "Total Tasks: 9" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_tasks_with_recurring_task() {
		tasks.addTask("Test");
		
		tasks.setRecurring(1, true);
		
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"Tasks on list '/default'",
				"",
				"R 1 - 'Test'",
				"",
				ANSI_BOLD + "Total Tasks: 1" + ANSI_RESET,
				""
		);
	}

	@Test
	void long_task_names_are_cutoff() {
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.startTask(2, false);
		tasks.startTask(3, true);

		tasks.setRecurring(1, true);

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(60);

		commands.execute(printStream, "list --tasks");

		assertOutput(
				"Tasks on list '/default'",
				"",
				"R 1 - 'Very long titles will be cut off at the side of t...'",
				"* " + ANSI_FG_GREEN + "3 - 'Very long titles will be cut off at the side of t...'" + ANSI_RESET,
				"  4 - 'Very long titles will be cut off at the side of t...'",
				"",
				ANSI_BOLD + "Total Tasks: 3" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void printing_tasks_in_group_when_there_are_no_tasks() {
		commands.execute(printStream, "list --tasks --group");
		
		assertOutput(
				"No tasks.",
				""
		);
	}
}