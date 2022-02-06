// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskGroup;

import java.io.IOException;
import java.io.PrintStream;

public class TaskGroupFileWriter {
	private final TaskGroup group;
	private final OSInterface osInterface;

	public TaskGroupFileWriter(TaskGroup group, OSInterface osInterface) {
		this.group = group;
		this.osInterface = osInterface;
	}

	public void write() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data/tasks" + group.getFullPath() + "group.txt"))) {
			outputStream.println("state " + group.getState());
			outputStream.println("time " + group.getTimeCategory());
			// TODO Notes
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
