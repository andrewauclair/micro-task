// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test {
	private final Tasks tasks = Mockito.spy(Tasks.class);
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final Commands commands = new Commands(tasks, new PrintStream(outputStream));
	
	@Test
	void execute_start_command() {
		tasks.addTask("com.andrewauclair.todo.Task 1");
		commands.execute("start 0");
		
		assertEquals("Started task 0 - \"com.andrewauclair.todo.Task 1\"" + System.lineSeparator(), outputStream.toString());
		
		Mockito.verify(tasks).startTask(0);
	}
	
	@Test
	void starting_second_task_stops_active_task() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		tasks.startTask(0);
		tasks.startTask(1);
		
		assertThat(tasks.getTasks().stream()
				.filter(task -> task.state == Task.TaskState.Active)).hasSize(1);
	}
}
