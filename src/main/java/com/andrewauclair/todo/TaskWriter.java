// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.IOException;
import java.io.OutputStream;

class TaskWriter {
	private final FileCreator fileCreator;

	public TaskWriter(FileCreator fileCreator) {

		this.fileCreator = fileCreator;
	}
	boolean writeTask(Task task, String fileName) {
		OutputStream outputStream;
		try {
			outputStream = fileCreator.createOutputStream(fileName);
			outputStream.write(task.task.getBytes());
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
