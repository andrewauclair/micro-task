// Copyright (C) 2019 Andrew Auclair - All Rights Reserved

package com.andrewauclair.todo.git;

import java.io.File;
import java.io.IOException;

public class GitControls {
	GitCommand addFile(String fileName) {
		return new GitCommand("git add " + fileName);
	}
	
	public void runCommand(GitCommand command) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File("git-data"));
		builder.command(command.toString().split(" "));
		builder.start();
	}
}
