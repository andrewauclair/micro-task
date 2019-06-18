// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

package com.andrewauclair.todo.git;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitControlsTest {
	@Test
	void git_add_file_command() {
		GitCommand command = new GitControls().addFile("test.txt");
		
		assertEquals("git add test.txt", command.toString());
	}
}
