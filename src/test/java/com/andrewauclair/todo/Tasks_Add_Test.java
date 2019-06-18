// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class Tasks_Add_Test {
	private final TaskWriter writer = Mockito.spy(TaskWriter.class);
	private final OSInterface osInterface = Mockito.spy(OSInterface.class);
	private final Tasks tasks = new Tasks(writer);
	
	@Test
	void adding_task_adds_it_to_a_list() {
		Task actualTask = tasks.addTask("Testing task add command");

		Task expectedTask = new Task(0, "Testing task add command");

		assertThat(tasks.getTasks()).containsOnly(expectedTask);
		assertEquals(expectedTask, actualTask);
	}

	@Test
	@Disabled
	void adding_task_tells_task_writer_to_write_file() {
		Task task = tasks.addTask("Testing task add command");

//		Mockito.verify(writer).writeTask(task1, "git-data/0.txt");
//		Mockito.verify(writer).writeTask(task2, "git-data/1.txt");
	}
}
