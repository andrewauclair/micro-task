// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

public class TaskGroupFinder {
	private final Tasks tasks;

	public TaskGroupFinder(Tasks tasks) {
		this.tasks = tasks;
	}

	public boolean hasGroupPath(TaskGroupName groupName) {
		return tasks.getRootGroup().getGroupAbsolute(groupName.absoluteName()).isPresent();
	}
}
