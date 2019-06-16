// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Stop_Test {
	private Tasks tasks = new Tasks();
	
	@Test
	void stop_command_sets_the_active_task_to_none() {
		tasks.addTask("Test 1");
		tasks.addTask("Test 2");
		
		tasks.startTask(1);
		
		assertEquals(new Tasks.Task(1, "Test 2"), tasks.getActiveTask());
		
		Tasks.Task stoppedTask = tasks.stopTask();
		
		ActiveTaskAsserts.assertNoActiveTask(tasks);
		
		assertEquals(new Tasks.Task(1, "Test 2"), stoppedTask);
	}
	
	@Test
	void stop_command_throws_exception_if_there_is_no_active_task() {
		ActiveTaskAsserts.assertThrowsNoActiveTaskException(tasks::stopTask);
	}
}
