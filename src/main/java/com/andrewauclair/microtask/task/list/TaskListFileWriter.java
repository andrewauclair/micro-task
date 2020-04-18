// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.list;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskList;

import java.io.IOException;
import java.io.PrintStream;

public class TaskListFileWriter {
	private final TaskList list;
	private final OSInterface osInterface;

	private String folder = "git-data";

	public TaskListFileWriter(TaskList list, OSInterface osInterface) {
		this.list = list;
		this.osInterface = osInterface;
	}

	public TaskListFileWriter inFolder(String folder) {
		this.folder = folder;
		return this;
	}

	public void write() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream(folder + "/tasks" + list.getFullPath() + "/list.txt"))) {
			outputStream.println(list.getProject());
			outputStream.println(list.getFeature());
			outputStream.println(list.getState());
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}