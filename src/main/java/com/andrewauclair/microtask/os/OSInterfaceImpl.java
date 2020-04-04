// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.Main;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.andrewauclair.microtask.os.ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterfaceImpl implements OSInterface {
	private Terminal terminal = null;

	private String lastInputFile = "";
	private Main main;
	private DataOutputStream statusOutput;

	public void setMain(Main main) {
		this.main = main;
	}

	public void createTerminal() throws IOException {
		if (terminal != null) {
			terminal.close();
		}
		terminal = TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.streams(System.in, System.out)
				.build();

		System.setIn(terminal.input());
		System.setOut(new PrintStream(terminal.output()));

		if (main != null) {
			main.newTerminal(terminal);
		}
	}

	public void openStatusLink() throws IOException {
		System.out.println("Waiting for client on " + InetAddress.getLocalHost() + ":5678");

		ServerSocket server = new ServerSocket(5678);
		Socket accept = server.accept();

		statusOutput = new DataOutputStream(accept.getOutputStream());
	}

	void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}

	@Override
	public boolean runGitCommand(String command, boolean print) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
		}

		boolean createdTerminal = false;

		try {
			// pause doesn't seem to work and we need input with the git commands
			// calling close and then rebuilding the terminal is the only thing that we can do
			if (terminal != null) {
				terminal.close();
				terminal = null;
			}

			ProcessBuilder pb = new ProcessBuilder();
			pb.directory(new File("git-data"));
			pb.command(command.split(" "));
			pb.redirectInput(Redirect.INHERIT);

			pb.redirectOutput(print ? Redirect.INHERIT : Redirect.DISCARD);
			pb.redirectError(Redirect.INHERIT);

			Process p = pb.start();

			int exitCode = p.waitFor();

			try {
				createTerminal();
				createdTerminal = true;
			}
			catch (IOException e) {
				e.printStackTrace();
			}

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
			if (!createdTerminal) {
				try {
					createTerminal();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
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

		if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
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

	@Override
	public String getEnvVar(String name) {
		return System.getenv(name);
	}

	@Override
	public void sendStatusMessage(StatusConsole.TransferType transferType) {
		try {
			statusOutput.write(transferType.ordinal());
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void sendStatusMessage(StatusConsole.TransferType transferType, String data) {
		try {
			statusOutput.write(transferType.ordinal());
			statusOutput.writeUTF(data);
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void copyToClipboard(String stringToCopy) {
		StringSelection stringSelection = new StringSelection(stringToCopy);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
}
