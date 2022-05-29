// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

import java.util.Optional;

public class TaskFinder {
	private final Tasks tasks;

	public TaskFinder(Tasks tasks) {
		this.tasks = tasks;
	}

	// TODO Is there a way that we could create a custom stream to do stuff like this where we loop through everything?
	public TaskList findListForTask(ExistingID id) {
		Optional<TaskList> listForTask = tasks.getRootGroup().findListForTask(id);
		if (listForTask.isEmpty()) {
			throw new TaskException("List for task " + id + " was not found.");
		}
		return listForTask.get();
	}
}
