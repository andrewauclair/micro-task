// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskGroup;

import java.io.IOException;
import java.io.PrintStream;

public class TaskGroupFileWriter {
	private final TaskGroup group;
	private final OSInterface osInterface;

	private String folder = "git-data";

	public TaskGroupFileWriter(TaskGroup group, OSInterface osInterface) {
		this.group = group;
		this.osInterface = osInterface;
	}

	public TaskGroupFileWriter inFolder(String folder) {
		this.folder = folder;
		return this;
	}

	public void write() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream(folder + "/tasks" + group.getFullPath() + "group.txt"))) {
			outputStream.println(group.getProject());
			outputStream.println(group.getFeature());
			outputStream.println(group.getState());
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
