// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_List_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_list_command() {
		tasks.addTask("Task 1");
		tasks.addTask("Task 2");
		tasks.addTask("Task 3");
		tasks.startTask(2);
		tasks.finishTask();
		tasks.startTask(3);
		
		String expected = "  1 - \"Task 1\"" + Utils.NL +
				"* 3 - \"Task 3\"" + Utils.NL;
		
		commands.execute("list");
		
		assertEquals(expected, outputStream.toString());
		
		Mockito.verify(tasks).getTasks();
		Mockito.verify(tasks).getActiveTask();
	}
	
	@Test
	void list_command_caps_at_20_tasks_and_displays_a_count_of_how_many_are_left() {
		IntStream.range(1, 40)
				.forEach(num -> tasks.addTask("Test " + num));
		
		String expected = "  1 - \"Test 1\"" + Utils.NL +
				"  2 - \"Test 2\"" + Utils.NL +
				"  3 - \"Test 3\"" + Utils.NL +
				"  4 - \"Test 4\"" + Utils.NL +
				"  5 - \"Test 5\"" + Utils.NL +
				"  6 - \"Test 6\"" + Utils.NL +
				"  7 - \"Test 7\"" + Utils.NL +
				"  8 - \"Test 8\"" + Utils.NL +
				"  9 - \"Test 9\"" + Utils.NL +
				"  10 - \"Test 10\"" + Utils.NL +
				"  11 - \"Test 11\"" + Utils.NL +
				"  12 - \"Test 12\"" + Utils.NL +
				"  13 - \"Test 13\"" + Utils.NL +
				"  14 - \"Test 14\"" + Utils.NL +
				"  15 - \"Test 15\"" + Utils.NL +
				"  16 - \"Test 16\"" + Utils.NL +
				"  17 - \"Test 17\"" + Utils.NL +
				"  18 - \"Test 18\"" + Utils.NL +
				"  19 - \"Test 19\"" + Utils.NL +
				"  20 - \"Test 20\"" + Utils.NL +
				"(19 more tasks.)" + Utils.NL;
		
		commands.execute("list");
		
		assertEquals(expected, outputStream.toString());
	}
	
	@Test
	void list_command_does_not_throw_the_no_active_task_exception() {
		commands.execute("list");
		
		assertEquals("No tasks." + Utils.NL, outputStream.toString());
	}
}
