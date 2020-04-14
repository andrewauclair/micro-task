// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.os.OSInterface;

public class GitHelper {
	private final OSInterface osInterface;

	// default to committing all files
	private String file = ".";

	public GitHelper(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	public GitHelper withFile(String file) {
		this.file = file;
		return this;
	}

	public void commit(String message) {
		osInterface.runGitCommand("git add " + file);
		osInterface.runGitCommand("git commit -m \"" + message + "\"");
	}
}
