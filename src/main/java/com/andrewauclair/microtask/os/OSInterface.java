// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.os;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;

public interface OSInterface {
	void gitCommit(String message);

	void gitPush();

	void gitPull();

	long currentSeconds();

	ZoneId getZoneId();

	DataOutputStream createOutputStream(String fileName) throws IOException;

	boolean canCreateFiles();

	InputStream createInputStream(String fileName) throws IOException;

	void removeFile(String fileName);

	List<TaskFileInfo> listFiles(String folder);

	int getTerminalHeight();

	int getTerminalWidth();

	String getVersion() throws IOException;

	void exit();

	void createFolder(String folder);

	void moveFolder(String src, String dest) throws IOException;

	String getLastInputFile();

	boolean fileExists(String fileName);

	String getEnvVar(String name);

	void sendStatusMessage(StatusConsole.TransferType transferType);

	void sendStatusMessage(StatusConsole.TransferType transferType, String data);

	void copyToClipboard(String stringToCopy);

	boolean promptChoice(String prompt);

	String promptForString(String prompt, Function<String, Boolean> isValid);

	final class TaskFileInfo {
		final String name;
		final String path;
		final boolean isDirectory;

		public TaskFileInfo(String name, String path, boolean isDirectory) {
			this.name = name;
			this.path = path;
			this.isDirectory = isDirectory;
		}

		public String getFileName() {
			return name;
		}

		public String getPath() {
			return path;
		}

		public boolean isDirectory() {
			return isDirectory;
		}
	}
}
