// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

import java.util.Optional;

public class TaskListFinder {
	private final Tasks tasks;

	public TaskListFinder(Tasks tasks) {
		this.tasks = tasks;
	}

	public boolean hasList(TaskListName list) {
		TaskGroup rootGroup = tasks.getRootGroup();

		String name = list.shortName();

		// strip root off path
		String path = list.parentGroupName().absoluteName().substring(1);

		TaskGroup group = rootGroup;

		while (path.endsWith("/")) {
			String groupPath = group.getFullPath() + path.substring(0, path.indexOf('/') + 1);
			Optional<TaskGroup> groupAbsolute = group.getGroupAbsolute(groupPath);

			if (groupAbsolute.isEmpty()) {
				throw new TaskException("Group '" + groupPath + "' does not exist.");
			}

			group = groupAbsolute.get();

			path = path.substring(path.indexOf('/') + 1);
		}

		return group.containsListAbsolute(list.absoluteName());
	}
}
