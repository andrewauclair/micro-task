// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Main;
import com.andrewauclair.todo.command.Commands;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterfaceImpl implements OSInterface {
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

	@Override
	public void setCommands(Commands commands) {
		this.commands = commands;
	}

	@Override
	public boolean runGitCommand(String command, boolean print) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
		}
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(new File("git-data"));
		builder.command(command.split(" "));
		
		if (commands.getDebugCommand().isDebugEnabled() || print) {
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

	@Override
	public long currentSeconds() {
		return Instant.now().getEpochSecond();
	}

	@Override
	public ZoneId getZoneId() {
		return ZoneId.systemDefault();
	}

	@Override
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

	@Override
	public InputStream createInputStream(String fileName) throws IOException {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use createInputStream in tests.");
		}
		return new FileInputStream(new File(fileName));
	}

	@Override
	public void removeFile(String fileName) {
		boolean delete = new File(fileName).delete();

		if (!delete) {
			throw new RuntimeException("Failed to delete " + fileName);
		}
	}
	
	@Override
	public List<TaskFileInfo> listFiles(String folder) {
		File[] files = new File(folder).listFiles();
		List<TaskFileInfo> info = new ArrayList<>();
		if (files != null) {
			for (File file : files) {
				info.add(new TaskFileInfo(file.getName(), file.getAbsolutePath(), file.isDirectory()));
			}
		}
		return info;
	}
	
	@Override
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

	@Override
	public String getVersion() throws IOException {
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("version.properties");
		Properties props = new Properties();
		if (resourceAsStream != null) {
			props.load(resourceAsStream);
		}

		return (String) props.get("version");
	}

	@Override
	public void exit() {
		System.exit(0);
	}

	@Override
	public void createFolder(String folder) {
		//noinspection ResultOfMethodCallIgnored
		new File(folder).mkdirs();
	}

	@Override
	public void moveFolder(String src, String dest) throws IOException {
		Files.move(new File("git-data/tasks" + src).toPath(), new File("git-data/tasks" + dest).toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}
