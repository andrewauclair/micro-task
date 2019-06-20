// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Commands;
import com.andrewauclair.todo.git.GitCommand;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterface {
	private boolean enableDebug = false;
	private Commands commands;
	
	public void setEnableDebug(boolean debug) {
		enableDebug = debug;
	}
	
	public void setCommands(Commands commands) {
		this.commands = commands;
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
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File("git-data"));
		builder.command(command.toString().split(" "));
		
		if (commands.isDebugEnabled()) {
			System.out.println("run: " + command);
			builder.inheritIO();
		}
		
		try {
			Process process = builder.start();
			process.waitFor();
		}
		catch (IOException | InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public long currentSeconds() {
		return Instant.now().getEpochSecond();
	}
}
