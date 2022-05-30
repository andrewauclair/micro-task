// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

public class NewID {
	private final long id;

	public NewID(IDValidator idValidator, long id) {
		this.id = id;

		if (idValidator.containsExistingID(id)) {
			throw new TaskException("Task with ID " + id + " already exists.");
		}
	}

	public long get() {
		return id;
	}
}
