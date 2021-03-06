// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

public class NewID {
	private final long id;

	public NewID(Tasks tasks, long id) {
		this.id = id;

		if (tasks.hasTaskWithID(id)) {
			throw new TaskException("Task " + id + " already exist.");
		}
	}

	public long get() {
		return id;
	}
}
