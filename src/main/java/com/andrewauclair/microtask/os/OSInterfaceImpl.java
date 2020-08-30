// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.LocalSettings;
import com.andrewauclair.microtask.Main;
import com.andrewauclair.microtask.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
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
import java.util.concurrent.TimeUnit;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterfaceImpl implements OSInterface {
	public static boolean disableGit = false;

	public Terminal terminal = null;

	private String lastInputFile = "";
	private DataOutputStream statusOutput;
	private LocalSettings localSettings;

	private final Git repo;// = Git.init().setDirectory(new File("git-data")).call();

	public OSInterfaceImpl() throws GitAPIException {
		if (!disableGit) {
			repo = initGitRepo();
		}
		else {
			repo = null;
		}
	}

	private Git initGitRepo() throws GitAPIException {
		final Git repo;
		File directory = new File("git-data");
		boolean exists = directory.exists();

		// if the directory doesn't exist JGit will create it for us
		repo = Git.init().setDirectory(directory).call();

		// write some info if the repo didn't exist
		if (!exists) {
			Utils.writeCurrentVersion(this);

			try (PrintStream outputStream = new PrintStream(createOutputStream("git-data/next-id.txt"))) {
				outputStream.print(1);
			}
			catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}

		String username = getEnvVar("username");

		// set the user.name and user.email every time we start
		repo.getRepository().getConfig().setString("user", null, "name", username);
		repo.getRepository().getConfig().setString("user", null, "email", username + "@" + getEnvVar("computername"));
		try {
			repo.getRepository().getConfig().save();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return repo;
	}

	public void setLocalSettings(LocalSettings localSettings) {
		this.localSettings = localSettings;
	}

	public Terminal terminal() throws IOException {
		return TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.nativeSignals(true)
				.build();
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

	private void printTimeDiff(long start, long stop) {
		System.out.println(TimeUnit.NANOSECONDS.toMillis(stop - start) + "ms");
	}

//	@Override
//	public boolean runGitAddAll() {
////		Git repo = new Git()
//		return false;
//	}

//	@Override
//	public boolean runGitCommit(String message) {
//		return false;
//	}

	@Override
	public void gitCommit(String message) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
		}

		if (disableGit) {
			return;
		}

		try {
			// add all files
			repo.add()
					.addFilepattern(".")
					.call();

			repo.commit()
					.setMessage(message)
					.call();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void gitPush() {
		try {
			repo.push().call();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void gitPull() {
		try {
			repo.pull().call();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	//	@Override
//	public boolean runGitCommand(String command) {
//		if (isJUnitTest()) {
//			throw new RuntimeException("Shouldn't use runGitCommand in tests.");
//		}
//
//		if (disableGit) {
//			return true;
//		}
//
//		try {
//			repo.commit()
//					.setMessage("Message")
//					.call();
//		}
//		catch (GitAPIException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			ProcessBuilder pb = new ProcessBuilder();
//			pb.directory(new File("git-data"));
//			pb.command(command.split(" "));
//			pb.redirectInput(Redirect.INHERIT);
//
//			pb.redirectOutput(localSettings.isDebugEnabled() ? Redirect.INHERIT : Redirect.DISCARD);
//			pb.redirectError(Redirect.INHERIT);
//
//			Process p = pb.start();
//
//			long start = System.nanoTime();
//
//			int exitCode = p.waitFor();
//
//			long afterWait = System.nanoTime();
//			if (localSettings.isDebugEnabled()) {
//				System.out.print("After Wait: ");
//				printTimeDiff(start, afterWait);
//			}
//
//			if (exitCode != 0) {
//				System.out.println();
//				System.out.println(ANSI_FG_RED + "Error while executing \"" + command + "\"" + ConsoleColors.ANSI_RESET);
//				System.out.println();
//			}
//		}
//		catch (InterruptedException | IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//
//		return true;
//	}

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
	public boolean canCreateFiles() {
		return true;
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
	public int getTerminalHeight() {
		int height = terminal.getHeight();

		if (height == 0) {
			height = 30;
		}
		return height;
	}

	@Override
	public int getTerminalWidth() {
		int width = terminal.getWidth();

		// must be running in IntelliJ
		if (width == 0) {
			width = 80;
		}
		return width;
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
