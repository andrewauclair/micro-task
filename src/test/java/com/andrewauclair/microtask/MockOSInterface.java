// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.os.StatusConsole;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MockOSInterface implements OSInterface {
	private long time = 1000;
	private boolean incrementTime = true;

	public void setTime(long time) {
		this.time = time;
	}

	public void setIncrementTime(boolean incrementTime) {
		this.incrementTime = incrementTime;
	}

	@Override
	public void gitCommit(String message) {
	}

	@Override
	public void gitPush() {
	}

	@Override
	public void gitPull() {
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

	@SuppressWarnings("RedundantThrows")
	@Override
	public DataOutputStream createOutputStream(String fileName) throws IOException {
		// throws IOException for tests even though it's not used
		return null;
	}

	@Override
	public boolean canCreateFiles() {
		return true;
	}

	@SuppressWarnings("RedundantThrows")
	@Override
	public InputStream createInputStream(String fileName) throws IOException {
		// throws IOException for tests even though it's not used
		return null;
	}

	@Override
	public void removeFile(String fileName) {
	}
	
	@Override
	public List<TaskFileInfo> listFiles(String folder) {
		return new ArrayList<>();
	}

	@Override
	public int getTerminalHeight() {
		return 20;
	}

	@Override
	public int getTerminalWidth() {
		return 60;
	}
	
	@SuppressWarnings("RedundantThrows")
	@Override
	public String getVersion() throws IOException {
		// needs to have throws IOException to pass tests even though it's unused
		return null;
	}

	@Override
	public void exit() {
	}

	@Override
	public void createFolder(String folder) {
	}

	@SuppressWarnings("RedundantThrows")
	@Override
	public void moveFolder(String src, String dest) throws IOException {
	}

	@Override
	public String getLastInputFile() {
		return "";
	}

	@Override
	public boolean fileExists(String fileName) {
		return false;
	}

	@Override
	public String getEnvVar(String name) {
		return "";
	}

	@Override
	public void sendStatusMessage(StatusConsole.TransferType transferType) {
	}

	@Override
	public void sendStatusMessage(StatusConsole.TransferType transferType, String data) {
	}

	@Override
	public void copyToClipboard(String stringToCopy) {
	}

	@Override
	public boolean promptChoice(String prompt) {
		return false;
	}

	@Override
	public String promptForString(String prompt, Function<String, Boolean> isValid) {
		return "";
	}
}
