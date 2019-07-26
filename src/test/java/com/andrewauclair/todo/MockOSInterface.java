// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;

public class MockOSInterface implements OSInterface {
	private long time = 1000;
	private boolean incrementTime = true;

	@Override
	public void setCommands(Commands commands) {

	}

	@Override
	public boolean runGitCommand(String command) {
		return false;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setIncrementTime(boolean incrementTime) {
		this.incrementTime = incrementTime;
	}

	@Override
	public long currentSeconds() {
		long currentTime = time;
		if (incrementTime) {
			time += 1000;
		}

		return currentTime;
	}

	@Override
	public ZoneId getZoneId() {
		return null;
	}

	@Override
	public DataOutputStream createOutputStream(String fileName) throws IOException {
		return null;
	}

	@Override
	public InputStream createInputStream(String fileName) throws IOException {
		return null;
	}

	@Override
	public void removeFile(String fileName) {

	}

	@Override
	public void clearScreen() {

	}

	@Override
	public String getVersion() throws IOException {
		return null;
	}

	@Override
	public void exit() {

	}
}
