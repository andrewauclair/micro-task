// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Commands_Rename_Test extends CommandsBaseTestCase {
	@Test
	void when_running_rename_command_the_app_will_output_the_current_name_as_a_string_to_edit() {
		tasks.addTask("Testing the rename of a task");
		
		commands.execute("rename 1 \"This is the new name of the task\"");
		
		assertEquals("Renamed task 1 - 'This is the new name of the task'" +
				Utils.NL + Utils.NL, outputStream.toString());
	}
}
