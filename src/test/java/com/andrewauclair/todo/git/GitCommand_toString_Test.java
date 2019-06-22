// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.git;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitCommand_toString_Test {
	@Test
	void toString_returns_command() {
		GitCommand command = new GitCommand("git init");

		assertEquals("git init", command.toString());
	}
}
