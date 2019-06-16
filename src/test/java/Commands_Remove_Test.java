// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Remove_Test {
	private Tasks tasks = new Tasks();
	
	@Test
	void removing_a_task_removes_it_from_the_task_list() {
		tasks.addTask("Testing tasks");
		tasks.addTask("Testing tasks 2");
		
		assertThat(tasks.getTasks()).containsOnly(new Tasks.Task(0, "Testing tasks"),
				new Tasks.Task(1, "Testing tasks 2"));
		
		tasks.finishTask(1);
		
		assertThat(tasks.getTasks()).containsOnly(new Tasks.Task(0, "Testing tasks"));
	}
}
