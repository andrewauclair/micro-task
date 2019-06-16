// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Commands_Add_Test {
	@Test
	void adding_task_adds_it_to_a_list() {
		Tasks.addTask("Testing task add command");
		
		assertThat(Tasks.getTasks()).containsOnly("Testing task add command");
	}
}
