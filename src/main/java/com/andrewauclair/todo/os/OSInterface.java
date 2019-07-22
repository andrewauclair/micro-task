// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Commands;
import com.andrewauclair.todo.Main;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Properties;

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
		
		if (commands.getDebugCommand().isDebugEnabled()) {
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
	
	public DataOutputStream createOutputStream(String fileName) throws IOException {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use createOutputStream in tests.");
		}
		File file = new File(fileName);
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			System.out.println("Failed to create directory: " + file.getParentFile());
		}
		return new DataOutputStream(new FileOutputStream(file));
	}

	public InputStream createInputStream(String fileName) throws IOException {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use createInputStream in tests.");
		}
		return new FileInputStream(new File(fileName));
	}

	public void removeFile(String fileName) {
		boolean delete = new File(fileName).delete();

		if (!delete) {
			throw new RuntimeException("Failed to delete " + fileName);
		}
	}

	public void clearScreen() {
		//Clears Screen in java
		try {
			if (System.getProperty("os.name").contains("Windows")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			}
			else {
				Runtime.getRuntime().exec("clear");
			}
		}
		catch (IOException | InterruptedException ignored) {
		}
	}

	public String getVersion() throws IOException {
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("version.properties");
		Properties props = new Properties();
		props.load(resourceAsStream);

		return (String) props.get("version");
	}

	public void exit() {
		System.exit(0);
	}
}
