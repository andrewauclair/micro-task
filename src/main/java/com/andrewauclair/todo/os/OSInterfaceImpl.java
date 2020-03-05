// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Main;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Status;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.andrewauclair.todo.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterfaceImpl implements OSInterface {
	private Terminal terminal;

	private String lastInputFile = "";
	private Main main;

	public void setMain(Main main) {
		this.main = main;
	}

	public void createTerminal() throws IOException {
		terminal = TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.streams(System.in, System.out)
				.build();

		System.setIn(terminal.input());
		System.setOut(new PrintStream(terminal.output()));

		main.newTerminal(terminal);
	}

	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}

	@Override
	public boolean runGitCommand(String command, boolean print) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
		}

		System.out.flush();

		try {
			// pause doesn't seem to work and we need input with the git commands
			// calling close and then rebuilding the terminal is the only thing that we can do
			terminal.close();

			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(new File("git-data"));
			pb.command(command.split(" "));
			pb.redirectInput(Redirect.INHERIT);

			pb.redirectOutput(print ? Redirect.INHERIT : Redirect.DISCARD);

			Process p = pb.start();

			int exitCode = p.waitFor();

			if (exitCode != 0) {
				System.out.println();
				System.out.println(ANSI_FG_RED + "Error while executing \"" + command + "\"" + ConsoleColors.ANSI_RESET);
				System.out.println();
			}
		}
		catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			// rebuild terminal
			try {
				createTerminal();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
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

	// copied from ExecHelper in jline so that we can customize it
	private static int waitAndCapture(Process p, boolean print) throws IOException, InterruptedException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (InputStream in = p.getInputStream(); InputStream err = p.getErrorStream(); OutputStream out = p.getOutputStream()) {
			int c;
			while ((c = in.read()) != -1) {
				bout.write(c);
			}

			while ((c = err.read()) != -1) {
				bout.write(c);
			}

			while ((c = err.read()) != -1) {
				bout.write(c);
			}

			int exitCode = p.waitFor();

			if (print || exitCode != 0) {
				System.out.println(bout.toString());
			}

			return exitCode;
		}
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
		File file = new File(fileName);
		lastInputFile = file.getAbsolutePath();
		return new FileInputStream(file);
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
	public int getTerminalWidth() {
		return terminal.getWidth();
	}

	@Override
	public String getVersion() throws IOException {
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("version.properties");
		Properties props = new Properties();
		if (resourceAsStream != null) {
			props.load(resourceAsStream);
		}

		String version = (String) props.get("version");

		if (version == null) {
			version = "Unknown";
		}
		return version;
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

	@Override
	public String getLastInputFile() {
		return lastInputFile;
	}

	@Override
	public boolean fileExists(String fileName) {
		return new File(fileName).exists();
	}
}
