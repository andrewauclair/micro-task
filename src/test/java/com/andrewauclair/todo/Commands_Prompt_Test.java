// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Prompt_Test extends CommandsBaseTestCase {
	@Test
	void on_start_up_the_prompt_is_default_list_and_none_active_task() {
		assertEquals("default - none>", commands.getPrompt());
	}

	@Test
	void switching_to_new_list_switches_the_list_text_on_prompt() {
		commands.execute("create-list test");
		commands.execute("switch-list test");

		assertEquals("test - none>", commands.getPrompt());
	}

	@Test
	void activating_a_task_displays_the_active_task_number_in_prompt() {
		tasks.addTask("Test");
		tasks.startTask(1);

		assertEquals("default - 1>", commands.getPrompt());
	}
}
