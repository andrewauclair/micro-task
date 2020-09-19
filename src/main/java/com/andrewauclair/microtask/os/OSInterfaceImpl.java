// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import com.andrewauclair.microtask.Main;
import com.andrewauclair.microtask.Utils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
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
import java.util.Set;

// Everything we can't really test will go here and we'll mock it in the tests and ignore this in the codecov
public class OSInterfaceImpl implements OSInterface {
	public static boolean disableGit = false;

	public Terminal terminal = null;

	private String lastInputFile = "";
	private DataOutputStream statusOutput;

	private final Git repo;

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

	@Override
	public void gitCommit(String message) {
		if (isJUnitTest()) {
			throw new RuntimeException("Shouldn't use gitCommit in tests.");
		}

		if (disableGit) {
			return;
		}

		try {
			Status status = repo.status()
					.call();

			// add all files
			AddCommand add = repo.add();

			Set<String> changes = status.getUncommittedChanges();
			changes.addAll(status.getUntracked());

			for (final String change : changes) {
				add.addFilepattern(change);
			}
			add.call();

			repo.commit()
					.setMessage(message)
					.call();
		}
		catch (GitAPIException e) {
			e.printStackTrace(System.out);
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
