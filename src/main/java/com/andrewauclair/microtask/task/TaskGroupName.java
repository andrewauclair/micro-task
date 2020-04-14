// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import java.util.Objects;

import static com.andrewauclair.microtask.task.TaskGroup.ROOT_PATH;

public class TaskGroupName {
	private final String absoluteName;
	private final String shortName;

	public TaskGroupName(Tasks tasks, String name) {
		if (!name.endsWith("/")) {
			throw new RuntimeException("Group name must end in /");
		}

		this.absoluteName = absoluteName(tasks, name);
		this.shortName = absoluteName.substring(absoluteName.lastIndexOf('/') + 1);
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

	public String parentGroupName() {
		return absoluteName.substring(0, absoluteName.lastIndexOf('/') + 1);
	}

	@Override
	public String toString() {
		return absoluteName;
	}
}
