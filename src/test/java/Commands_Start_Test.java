// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Start_Test {
	Tasks tasks = new Tasks();
	
	@Test
	void starting_task_assigns_it_as_the_active_task() {
		int id = tasks.addTask("Testing task start command");
		
		tasks.startTask(id);
		
		assertEquals(id, tasks.getActiveTask());
	}
}
