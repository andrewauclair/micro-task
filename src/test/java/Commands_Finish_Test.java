// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Commands_Finish_Test {
	private Tasks tasks = new Tasks();
	
	@Test
	void finishing_a_task_removes_it_from_the_task_list() {
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
		
		assertThat(tasks.getTasks()).containsOnly(new Tasks.Task(0, "Testing tasks"),
				new Tasks.Task(1, "Testing tasks 2"));
		
		tasks.finishTask(1);
		
		assertThat(tasks.getTasks()).containsOnly(new Tasks.Task(0, "Testing tasks"));
	}
	
	@Test
	void finishing_non_existent_id_throws_exception_with_message() {
		RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> tasks.finishTask(5));
		
		assertEquals("Task 5 was not found.", runtimeException.getMessage());
	}
}
