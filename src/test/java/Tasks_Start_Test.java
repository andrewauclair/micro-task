// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Tasks_Start_Test {
	private final Tasks tasks = new Tasks();

	@Test
	void starting_task_assigns_it_as_the_active_task() {
		tasks.addTask("Empty task");
		int id = tasks.addTask("Testing task start command");

		Task newActiveTask = tasks.startTask(id);

		assertEquals(new Task(1, "Testing task start command"), tasks.getActiveTask());
		assertEquals(tasks.getActiveTask(), newActiveTask);
	}

	@Test
	void starting_non_existent_id_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.startTask(5));

		assertEquals("Task 5 was not found.", runtimeException.getMessage());
	}
}
