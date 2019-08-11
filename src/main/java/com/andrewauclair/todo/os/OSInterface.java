package com.andrewauclair.todo.os;

import com.andrewauclair.todo.command.Commands;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.List;

public interface OSInterface {
	void setCommands(Commands commands);

	boolean runGitCommand(String command);

	long currentSeconds();

	ZoneId getZoneId();

	DataOutputStream createOutputStream(String fileName) throws IOException;

	InputStream createInputStream(String fileName) throws IOException;

	void removeFile(String fileName);
	
	List<TaskFileInfo> listFiles(String folder);
	
	void clearScreen();

	String getVersion() throws IOException;

	void exit();
	
	class TaskFileInfo {
		String name;
		String path;
		boolean isDirectory;
		
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
