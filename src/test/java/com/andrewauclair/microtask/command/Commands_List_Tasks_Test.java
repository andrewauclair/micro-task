// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command;

import com.andrewauclair.microtask.os.ConsoleColors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.stream.IntStream;

import static com.andrewauclair.microtask.os.ConsoleColors.*;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GRAY;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleBackgroundColor.ANSI_BG_GREEN;
import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN;

class Commands_List_Tasks_Test extends CommandsBaseTestCase {
	@Test
	void execute_list_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(existingID(2), false);
		tasks.finishTask();
		tasks.startTask(existingID(3), false);
		
		commands.execute(printStream, "list --tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "       1  Task 1     " + ANSI_RESET,
				ANSI_BG_GREEN + "*      3  Task 3     " + ANSI_RESET,
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
		tasks.startTask(existingID(2), false);
		tasks.finishTask();
		tasks.startTask(existingID(3), false);
		
		tasks.addList(newList("test"), true);
		tasks.setActiveList(existingList("test"));
		
		commands.execute(printStream, "list --tasks --list default");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "       1  Task 1     " + ANSI_RESET,
				ANSI_BG_GREEN + "*      3  Task 3     " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 2" + ANSI_RESET,
				""
		);
	}

	@Test
	void display_tasks_in_a_different_group() {
		tasks.createGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/two"), true);

		tasks.addTask("Task 1", existingList("/test/one"));
		tasks.addTask("Task 2", existingList("/test/one"));
		tasks.addTask("Task 3", existingList("/test/one"));
		tasks.addTask("Task 4", existingList("/test/two"));
		tasks.addTask("Task 5", existingList("/test/two"));

		tasks.startTask(existingID(3), false);

		commands.execute(printStream, "list --tasks --group /test/");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in group '/test/'",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "one          1  Task 1     " + ANSI_RESET,
				"one          2  Task 2     ",
				ANSI_BG_GREEN + "one   *      3  Task 3     " + ANSI_RESET,
				"two          4  Task 4     ",
				ANSI_BG_GRAY + "two          5  Task 5     " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 5" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void display_finished_tasks() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		
		tasks.finishTask(existingID(1));
		tasks.finishTask(existingID(3));
		
		commands.execute(printStream, "list --tasks --finished");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Finished tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "  F    1  Task 1     " + ANSI_RESET,
				"  F    3  Task 3     ",
				"",
				ANSI_BOLD + "Total Finished Tasks: 2" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void display_finished_tasks_in_group() {
		tasks.createGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.addList(newList("/test/three"), true);
		tasks.addTask("Test 1", existingList("/test/one"));
		tasks.addTask("Test 2", existingList("/test/one"));
		tasks.addTask("Test 3", existingList("/test/one"));
		
		tasks.addTask("Test 4", existingList("/test/three"));
		tasks.addTask("Test 5", existingList("/test/three"));
		tasks.addTask("Test 6", existingList("/test/three"));
		
		tasks.finishTask(existingID(2));
		tasks.finishTask(existingID(5));
		
		tasks.setActiveGroup(existingGroup("/test/"));
		
		commands.execute(printStream, "list --tasks --current-group --finished");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in group '/test/'",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "one      F    2  Test 2     " + ANSI_RESET,
				"three    F    5  Test 5     ",
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

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "       1  Test 1     " + ANSI_RESET,
				"       2  Test 2     ",
				ANSI_BG_GRAY + "       3  Test 3     " + ANSI_RESET,
				"       4  Test 4     ",
				ANSI_BG_GRAY + "       5  Test 5     " + ANSI_RESET,
				"       6  Test 6     ",
				ANSI_BG_GRAY + "       7  Test 7     " + ANSI_RESET,
				"       8  Test 8     ",
				ANSI_BG_GRAY + "       9  Test 9     " + ANSI_RESET,
				"      10  Test 10    ",
				ANSI_BG_GRAY + "      11  Test 11    " + ANSI_RESET,
				"      12  Test 12    ",
				ANSI_BG_GRAY + "      13  Test 13    " + ANSI_RESET,
				"      14  Test 14    ",
				ANSI_BG_GRAY + "      15  Test 15    " + ANSI_RESET,
				"      16  Test 16    ",
				ANSI_BG_GRAY + "      17  Test 17    " + ANSI_RESET,
				"      18  Test 18    ",
				ANSI_BG_GRAY + "      19  Test 19    " + ANSI_RESET,
				"      20  Test 20    ",
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
		
		tasks.startTask(existingID(2), false);
		
		commands.execute(printStream, "list --tasks --all");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "       1  Test 1     " + ANSI_RESET,
				ANSI_BG_GREEN + "*      2  Test 2     " + ANSI_RESET,
				ANSI_BG_GRAY + "       3  Test 3     " + ANSI_RESET,
				"       4  Test 4     ",
				ANSI_BG_GRAY + "       5  Test 5     " + ANSI_RESET,
				"       6  Test 6     ",
				ANSI_BG_GRAY + "       7  Test 7     " + ANSI_RESET,
				"       8  Test 8     ",
				ANSI_BG_GRAY + "       9  Test 9     " + ANSI_RESET,
				"      10  Test 10    ",
				ANSI_BG_GRAY + "      11  Test 11    " + ANSI_RESET,
				"      12  Test 12    ",
				ANSI_BG_GRAY + "      13  Test 13    " + ANSI_RESET,
				"      14  Test 14    ",
				ANSI_BG_GRAY + "      15  Test 15    " + ANSI_RESET,
				"      16  Test 16    ",
				ANSI_BG_GRAY + "      17  Test 17    " + ANSI_RESET,
				"      18  Test 18    ",
				ANSI_BG_GRAY + "      19  Test 19    " + ANSI_RESET,
				"      20  Test 20    ",
				ANSI_BG_GRAY + "      21  Test 21    " + ANSI_RESET,
				"      22  Test 22    ",
				"",
				ANSI_BOLD + "Total Tasks: 22" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_tasks_on_a_nested_list() {
		tasks.addGroup(newGroup("/test/"));
		tasks.addList(newList("/test/one"), true);
		tasks.setActiveGroup(existingGroup("/test/"));
		tasks.setActiveList(existingList("/test/one"));
		
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(existingID(2), false);
		tasks.finishTask();
		tasks.startTask(existingID(3), false);
		
		commands.execute(printStream, "list --tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/test/one'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "       1  Task 1     " + ANSI_RESET,
				ANSI_BG_GREEN + "*      3  Task 3     " + ANSI_RESET,
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
		tasks.createGroup(newGroup("/test/"));
		tasks.createGroup(newGroup("/test/junk/"));
		tasks.setActiveGroup(existingGroup("/test/"));
		
		tasks.addList(newList("/test/default"), true);
		tasks.setActiveList(existingList("/test/default"));
		
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(existingID(2), false);
		tasks.finishTask();
		tasks.startTask(existingID(3), false);
		
		tasks.addList(newList("one"), true);
		tasks.addList(newList("two"), true);

		tasks.setActiveList(existingList("one"));
		tasks.addTask("Task 4");
		tasks.addTask("Task 5");
		tasks.addTask("Task 6");

		tasks.setActiveList(existingList("two"));
		tasks.addTask("Task 7");
		tasks.addTask("Task 8");
		tasks.addTask("Task 9");
		
		// add extra tasks that shouldn't be shown
		tasks.setActiveList(existingList("/default"));
		tasks.addTask("Hidden 1");
		tasks.addTask("Hidden 2");
		
		tasks.addList(newList("/hide"), true);
		tasks.setActiveList(existingList("/hide"));
		
		tasks.addTask("Hidden 3");
		
//		outputStream.reset();
		
		commands.execute(printStream, "list --tasks --current-group");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in group '/test/'",
				"",
				u + "List" + r + "  " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "default         1  Task 1     " + ANSI_RESET,
				ANSI_BG_GREEN + "default  *      3  Task 3     " + ANSI_RESET,
				ANSI_BG_GRAY + "one             4  Task 4     " + ANSI_RESET,
				"one             5  Task 5     ",
				ANSI_BG_GRAY + "one             6  Task 6     " + ANSI_RESET,
				"two             7  Task 7     ",
				ANSI_BG_GRAY + "two             8  Task 8     " + ANSI_RESET,
				"two             9  Task 9     ",
				"",
				ANSI_BOLD + "Total Tasks: 8" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void print_tasks_in_group_recursively() {
		tasks.addGroup(newGroup("/one/two/"));
		tasks.addList(newList("/one/two/three"), true);
		tasks.addList(newList("/one/four"), true);
		tasks.setActiveList(existingList("/one/two/three"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		tasks.setActiveList(existingList("/one/four"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		tasks.addGroup(newGroup("/one/two/five/"));
		tasks.addList(newList("/one/two/five/nine"), true);
		tasks.setActiveList(existingList("/one/two/five/nine"));
		tasks.addTask("Test");
		tasks.addTask("Test");
		tasks.addTask("Test");

		tasks.setActiveGroup(existingGroup("/one/"));

		tasks.setRecurring(existingID(2), true);
		tasks.setRecurring(existingID(5), true);
		tasks.setRecurring(existingID(8), true);

		commands.execute(printStream, "list --tasks --current-group --recursive");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks in group '/one/'",
				"",
				u + "List" + r + "   " + u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r,
				ANSI_BG_GRAY + "two/three             1  Test       " + ANSI_RESET,
				"two/three       R     2  Test       ",
				ANSI_BG_GRAY + "two/three             3  Test       " + ANSI_RESET,
				"two/five/nine         7  Test       ",
				ANSI_BG_GRAY + "two/five/nine   R     8  Test       " + ANSI_RESET,
				"two/five/nine         9  Test       ",
				ANSI_BG_GRAY + "four                  4  Test       " + ANSI_RESET,
				"four            R     5  Test       ",
				ANSI_BG_GRAY + "four                  6  Test       " + ANSI_RESET,
				"",
				ANSI_BOLD + "Total Tasks: 9 (3 Recurring)" + ANSI_RESET,
				""
		);
	}
	
	@Test
	void list_tasks_with_recurring_task() {
		tasks.addTask("Test");
		
		tasks.setRecurring(existingID(1), true);
		
		commands.execute(printStream, "list --tasks");

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

	@Test
	void long_task_names_are_cutoff() {
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Very long titles will be cut off at the side of the screen so that they do not wrap around and mess with the times");
		tasks.addTask("Normal task");
		tasks.startTask(existingID(2), false);
		tasks.startTask(existingID(3), true);

		tasks.setRecurring(existingID(1), true);

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(60);

		commands.execute(printStream, "list --tasks");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r + "               ",
				ANSI_BG_GRAY + " R     1  Very long titles will be cut off at the side of..." + ANSI_RESET,
				ANSI_BG_GREEN + "*      3  Very long titles will be cut off at the side of..." + ANSI_RESET,
				ANSI_BG_GRAY + "       4  Very long titles will be cut off at the side of..." + ANSI_RESET,
				"       5  Normal task                                       ",
				"",
				ANSI_BOLD + "Total Tasks: 4 (1 Recurring)" + ANSI_RESET,
				""
		);
	}

	@Test
	void long_task_names_wrap_when_using_verbose() {
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");
		tasks.addTask("Very long titles will be wrapped at the side of the screen instead of being cut off at the edge");
		tasks.addTask("Normal task");
		tasks.startTask(existingID(2), false);
		tasks.startTask(existingID(3), true);

		tasks.setRecurring(existingID(1), true);

		Mockito.when(osInterface.getTerminalWidth()).thenReturn(60);

		commands.execute(printStream, "list --tasks -v");

		String u = ConsoleColors.ANSI_UNDERLINE;
		String r = ANSI_RESET;

		assertOutput(
				"Tasks on list '/default'",
				"",
				u + "Type" + r + "  " + u + "ID" + r + "  " + u + "Description" + r + "               ",
				ANSI_BG_GRAY + " R     1  Very long titles will be wrapped at the side of   ",
				 "          the screen instead of being cut off at the edge   " + ANSI_RESET,
				ANSI_BG_GREEN + "*      3  Very long titles will be wrapped at the side of   ",
				"          the screen instead of being cut off at the edge   " + ANSI_RESET,
				ANSI_BG_GRAY + "       4  Very long titles will be wrapped at the side of   ",
				"          the screen instead of being cut off at the edge   " + ANSI_RESET,
				"       5  Normal task                                       ",
				"",
				ANSI_BOLD + "Total Tasks: 4 (1 Recurring)" + ANSI_RESET,
				""
		);
	}

	@Test
	void printing_tasks_in_group_when_there_are_no_tasks() {
		commands.execute(printStream, "list --tasks --current-group");
		
		assertOutput(
				"Tasks in group '/'",
				"",
				"No tasks.",
				""
		);
	}
}
