// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;

import static com.andrewauclair.microtask.task.TaskGroup.ROOT_PATH;

// TODO Make this abstract, I use it in way too many places to do that right away
public class TaskListName {
	private final String absoluteName;
	private final String shortName;

	private final ExistingTaskGroupName parent;

	public TaskListName(Tasks tasks, String name) {
		if (name.endsWith("/")) {
			throw new RuntimeException("List name must not end in /");
		}

		this.absoluteName = absoluteName(tasks, name).toLowerCase();
		this.shortName = absoluteName.substring(absoluteName.lastIndexOf('/') + 1);

		String parent = absoluteName.substring(0, absoluteName.lastIndexOf('/') + 1);

		this.parent = new ExistingTaskGroupName(tasks, parent);
	}

	private String absoluteName(Tasks tasks, String name) {
		if (!name.startsWith(ROOT_PATH)) {
			return tasks.getActiveGroup().getFullPath() + name;
		}
		return name;
	}

	// TODO Not sure how long we might need to keep this around
	public String absoluteName() {
		return absoluteName;
	}

	public String shortName() {
		return shortName;
	}

	public ExistingTaskGroupName parentGroupName() {
		return parent;
	}

	@Override
	public String toString() {
		return absoluteName;
	}
}
