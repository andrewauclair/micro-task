// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Add_Test {
	private Tasks tasks = new Tasks();
	
	@Test
	void adding_task_adds_it_to_a_list() {
		tasks.addTask("Testing task add command");
		
		assertThat(tasks.getTasks()).containsOnly(new Task(0, "Testing task add command"));
	}
}
