// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Task_ID_Test extends TaskBaseTestCase {
	@Test
	void task_id_starts_at_1() {
		Task test = tasks.addTask("Test");

		assertEquals(1, test.id);
	}

	@Test
	void task_id_can_be_set_in_the_constructor() throws IOException {
		OSInterface osInterface = Mockito.mock(OSInterface.class);

		Mockito.when(osInterface.createOutputStream(Mockito.anyString())).thenReturn(new ByteArrayOutputStream());

		TaskWriter taskWriter = Mockito.mock(TaskWriter.class);
		Tasks tasks = new Tasks(5, taskWriter, new PrintStream(new ByteArrayOutputStream()), osInterface);

		Task test = tasks.addTask("Test");

		assertEquals(5, test.id);
	}
}
