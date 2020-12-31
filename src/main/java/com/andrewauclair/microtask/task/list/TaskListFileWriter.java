// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.list;

import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.task.TaskList;

import java.io.IOException;
import java.io.PrintStream;

public class TaskListFileWriter {
	private final TaskList list;
	private final OSInterface osInterface;

	public TaskListFileWriter(TaskList list, OSInterface osInterface) {
		this.list = list;
		this.osInterface = osInterface;
	}

	public void write() {
		try (PrintStream outputStream = new PrintStream(osInterface.createOutputStream("git-data" + "/tasks" + list.getFullPath() + "/list.txt"))) {
			outputStream.println(list.getState());
			// TODO Notes
		}
		catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
