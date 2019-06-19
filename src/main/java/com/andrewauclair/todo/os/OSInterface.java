// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.git.GitCommand;

import java.io.File;
import java.io.IOException;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterface {
	private boolean enableDebug = false;
	
	public void setEnableDebug(boolean debug) {
		enableDebug = debug;
	}
	
	private static boolean isJUnitTest() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			if (element.getClassName().startsWith("org.junit.")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean runGitCommand(GitCommand command) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
		}
		System.out.println("run: " + command);
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File("git-data"));
		builder.command(command.toString().split(" "));
		builder.inheritIO();
		try {
			Process process = builder.start();
			process.waitFor();
		}
		catch (IOException | InterruptedException e) {
			return false;
		}
		return true;
	}
}
