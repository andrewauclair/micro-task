// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_List_Test {
	private static final String NL = System.lineSeparator();
	
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));

	@Test
	void execute_list_command() {
		tasks.addTask("com.andrewauclair.todo.Task 1");
		tasks.addTask("com.andrewauclair.todo.Task 2");
		tasks.addTask("com.andrewauclair.todo.Task 3");
		tasks.startTask(2);
		tasks.finishTask();
		tasks.startTask(3);
		
		String expected = "  1 - \"com.andrewauclair.todo.Task 1\"" + NL +
				"* 3 - \"com.andrewauclair.todo.Task 3\"" + System.lineSeparator();

		commands.execute("list");

		assertEquals(expected, outputStream.toString());

		//noinspection ResultOfMethodCallIgnored
		Mockito.verify(tasks).getTasks();
		Mockito.verify(tasks).getActiveTask();
	}
	
	@Test
	void list_command_caps_at_20_tasks_and_displays_a_count_of_how_many_are_left() {
		IntStream.range(1, 40)
				.forEach(num -> tasks.addTask("Test " + num));
		
		String expected = "  1 - \"Test 1\"" + NL +
				"  2 - \"Test 2\"" + NL +
				"  3 - \"Test 3\"" + NL +
				"  4 - \"Test 4\"" + NL +
				"  5 - \"Test 5\"" + NL +
				"  6 - \"Test 6\"" + NL +
				"  7 - \"Test 7\"" + NL +
				"  8 - \"Test 8\"" + NL +
				"  9 - \"Test 9\"" + NL +
				"  10 - \"Test 10\"" + NL +
				"  11 - \"Test 11\"" + NL +
				"  12 - \"Test 12\"" + NL +
				"  13 - \"Test 13\"" + NL +
				"  14 - \"Test 14\"" + NL +
				"  15 - \"Test 15\"" + NL +
				"  16 - \"Test 16\"" + NL +
				"  17 - \"Test 17\"" + NL +
				"  18 - \"Test 18\"" + NL +
				"  19 - \"Test 19\"" + NL +
				"  20 - \"Test 20\"" + NL +
				"(19 more tasks.)" + NL;
		
		commands.execute("list");
		
		assertEquals(expected, outputStream.toString());
	}
	
	@Test
	void list_command_does_not_throw_the_no_active_task_exception() {
		commands.execute("list");

		assertEquals("No tasks." + System.lineSeparator(), outputStream.toString());
	}
}
