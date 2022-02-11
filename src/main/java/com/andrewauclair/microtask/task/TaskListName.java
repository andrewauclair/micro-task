// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.task.group.name.ExistingGroupName;

import java.util.Arrays;
import java.util.List;

import static com.andrewauclair.microtask.task.TaskGroup.ROOT_PATH;

public abstract class TaskListName {
	private static final List<Character> illegal_characters = Arrays.asList('\\', '<', '>', ':', '\"', '\\', '|', '?', '*');

	private final String absoluteName;
	private final String shortName;

	private final ExistingGroupName parent;

	public TaskListName(Tasks tasks, String name) {
		if (name.endsWith("/")) {
			throw new RuntimeException("List name must not end in /");
		}

		for (final char character : illegal_characters) {
			if (name.contains(String.valueOf(character))) {
				throw new RuntimeException("Illegal character in list name: '" + character + "'");
			}
		}

		this.absoluteName = absoluteName(tasks, name).toLowerCase();
		this.shortName = absoluteName.substring(absoluteName.lastIndexOf('/') + 1);

		String parent = absoluteName.substring(0, absoluteName.lastIndexOf('/') + 1);

		this.parent = new ExistingGroupName(tasks, parent);
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

	public ExistingGroupName parentGroupName() {
		return parent;
	}

	@Override
	public String toString() {
		return absoluteName;
	}
}
