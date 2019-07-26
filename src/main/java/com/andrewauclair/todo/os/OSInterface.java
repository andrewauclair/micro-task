package com.andrewauclair.todo.os;

import com.andrewauclair.todo.Commands;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;

public interface OSInterface {
	void setCommands(Commands commands);

	boolean runGitCommand(String command);

	long currentSeconds();

	ZoneId getZoneId();

	DataOutputStream createOutputStream(String fileName) throws IOException;

	InputStream createInputStream(String fileName) throws IOException;

	void removeFile(String fileName);

	void clearScreen();

	String getVersion() throws IOException;

	void exit();
}
