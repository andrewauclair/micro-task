// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Commands;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterface {
	private Commands commands;

	private static boolean isJUnitTest() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			if (element.getClassName().startsWith("org.junit.")) {
				return true;
			}
		}
		return false;
	}

	public void setCommands(Commands commands) {
		this.commands = commands;
	}

	public boolean runGitCommand(String command) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
		}
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File("git-data"));
		builder.command(command.split(" "));

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

	public ZoneId getZoneId() {
		return ZoneId.systemDefault();
	}

	public OutputStream createOutputStream(String fileName) throws IOException {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use createOutputStream in tests.");
		}
		File file = new File(fileName);
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			System.out.println("Failed to create directory: " + file.getParentFile());
		}
		return new FileOutputStream(file);
	}

	public InputStream createInputStream(String fileName) throws IOException {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use createInputStream in tests.");
		}
		return new FileInputStream(new File(fileName));
	}
}
