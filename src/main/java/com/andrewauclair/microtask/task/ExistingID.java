// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

public class ExistingID {
	private final long id;

	public ExistingID(Tasks tasks, long id) {
		this.id = id;
		TaskFinder finder = new TaskFinder(tasks);

		if (!finder.hasTaskWithID(id)) {
			throw new TaskException("Task " + id + " does not exist.");
		}
	}

	public long get() {
		return id;
	}
}
