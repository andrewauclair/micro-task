// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;

class Commands_Git_Test extends CommandsBaseTestCase {
	@Test
	void execute_git_pass_through_command() {
		commands.execute(printStream, "git push");
		
		Mockito.verify(osInterface, times(1)).runGitCommand("git push", true);
	}
}
