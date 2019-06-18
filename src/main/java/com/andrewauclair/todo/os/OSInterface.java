// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.git.GitCommand;

import java.io.File;
import java.io.IOException;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterface {
	public void runGitCommand(GitCommand command) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File("git-data"));
		builder.command(command.toString().split(" "));
		builder.start();
	}
}
