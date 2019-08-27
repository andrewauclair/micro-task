// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Prompt_Test extends CommandsBaseTestCase {
	@Test
	void on_start_up_the_prompt_is_default_list_and_none_active_task() {
		assertEquals("/default - none>", commands.getPrompt());
	}

	@Test
	void switching_to_new_list_switches_the_list_text_on_prompt() {
		commands.execute(printStream, "mklist test");
		commands.execute(printStream, "chlist test");

		assertEquals("/test - none>", commands.getPrompt());
	}

	@Test
	void activating_a_task_displays_the_active_task_number_in_prompt() {
		tasks.addTask("Test");
		tasks.startTask(1, false);

		assertEquals("/default - 1>", commands.getPrompt());
	}
	
	@Test
	void changing_to_group_other_than_the_active_lists_group_changes_prompt() {
		tasks.addList("/test/one/two");
		tasks.addList("/test/three");
		tasks.setActiveList("/test/three");
		
		tasks.switchGroup("/test/one/");
		
		assertEquals("/test/one/ - none>", commands.getPrompt());
	}
}
