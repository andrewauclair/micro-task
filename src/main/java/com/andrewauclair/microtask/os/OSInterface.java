package com.andrewauclair.microtask.os;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.List;

public interface OSInterface {
	boolean runGitCommand(String command, boolean print);

	long currentSeconds();

	ZoneId getZoneId();

	DataOutputStream createOutputStream(String fileName) throws IOException;

	InputStream createInputStream(String fileName) throws IOException;

	void removeFile(String fileName);

	List<TaskFileInfo> listFiles(String folder);

	void clearScreen();

	int getTerminalWidth();

	String getVersion() throws IOException;

	void exit();

	void createFolder(String folder);

	void moveFolder(String src, String dest) throws IOException;

	String getLastInputFile();

	boolean fileExists(String fileName);

	String getEnvVar(String name);

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
