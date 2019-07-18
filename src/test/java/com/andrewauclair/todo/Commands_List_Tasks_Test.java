// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_List_Tasks_Test extends CommandsBaseTestCase {
	@Test
	void execute_list_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2);
		tasks.finishTask();
		tasks.startTask(3);
		
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"  1 - 'Task 1'",
				"* " + ANSI_FG_GREEN + "3 - 'Task 3'" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void display_tasks_on_a_different_list() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2);
		tasks.finishTask();
		tasks.startTask(3);
		
		tasks.addList("test");
		tasks.setCurrentList("test");
		
		commands.execute(printStream, "list --tasks --list default");
		
		assertOutput(
				"Tasks on list 'default'",
				"",
				"  1 - 'Task 1'",
				"* " + ANSI_FG_GREEN + "3 - 'Task 3'" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_command_caps_at_20_tasks_and_displays_a_count_of_how_many_are_left() {
		IntStream.range(1, 40)
				.forEach(num -> tasks.addTask("Test " + num));
		
		commands.execute(printStream, "list --tasks");
		
		assertOutput(
				"  1 - 'Test 1'",
				"  2 - 'Test 2'",
				"  3 - 'Test 3'",
				"  4 - 'Test 4'",
				"  5 - 'Test 5'",
				"  6 - 'Test 6'",
				"  7 - 'Test 7'",
				"  8 - 'Test 8'",
				"  9 - 'Test 9'",
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
				""
		);
	}
	
	@Test
	void list_command_shows_all_tasks_when_all_parameter_is_passed() {
		IntStream.range(1, 23)
				.forEach(num -> tasks.addTask("Test " + num));
		
		// TODO This should also accept "list -ta"
		commands.execute(printStream, "list --tasks --all");
		
		assertOutput(
				"  1 - 'Test 1'",
				"  2 - 'Test 2'",
				"  3 - 'Test 3'",
				"  4 - 'Test 4'",
				"  5 - 'Test 5'",
				"  6 - 'Test 6'",
				"  7 - 'Test 7'",
				"  8 - 'Test 8'",
				"  9 - 'Test 9'",
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
				""
		);
	}
	
	@Test
	void list_command_does_not_throw_the_no_active_task_exception() {
		commands.execute(printStream, "list --tasks");
		
		assertEquals("No tasks." + Utils.NL + Utils.NL, outputStream.toString());
	}
	
	@Test
	void list_with_no_tasks_flag_is_invalid() {
		commands.execute(printStream, "list");
		
		assertOutput(
				"Invalid command.",
				""
		);
	}
}