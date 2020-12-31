// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
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

		this.absoluteName = absoluteName(tasks, name).toLowerCase();

		if (!Objects.equals(absoluteName, ROOT_PATH)) {
			String nameWithoutSlash = absoluteName.substring(0, absoluteName.length() - 1);
			this.shortName = nameWithoutSlash.substring(nameWithoutSlash.lastIndexOf('/') + 1);
		}
		else {
			this.shortName = "";
		}
	}

	private String absoluteName(Tasks tasks, String name) {
		if (!name.startsWith(ROOT_PATH)) {
			return tasks.getCurrentGroup().getFullPath() + name;
		}
		return name;
	}

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
