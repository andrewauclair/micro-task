// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test {
	@Test
	void starting_task_assigns_it_as_the_active_task() {
		int id = Tasks.addTask("Testing task start command");
		
		Tasks.startTask(id);
		
		assertEquals(id, Tasks.getActiveTask());
	}
}
