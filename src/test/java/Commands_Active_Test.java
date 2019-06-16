// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Active_Test {
	private Tasks tasks = new Tasks();
	
	@Test
	void returns_active_task() {
		tasks.addTask("Testing 1");
		tasks.addTask("Testing 2");
		
		tasks.startTask(1);
		
		assertEquals(new Tasks.Task(1, "Testing 2"), tasks.getActiveTask());
	}
}
