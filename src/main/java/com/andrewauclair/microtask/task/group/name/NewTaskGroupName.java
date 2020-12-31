// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group.name;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.task.TaskGroupFinder;
import com.andrewauclair.microtask.task.TaskGroupName;
import com.andrewauclair.microtask.task.Tasks;

import java.util.Objects;

public class NewTaskGroupName extends TaskGroupName {
	public NewTaskGroupName(Tasks tasks, String name) {
		super(tasks, name);

		TaskGroupFinder finder = new TaskGroupFinder(tasks);

		if (finder.hasGroupPath(this)) {
			throw new TaskException("Group '" + absoluteName() + "' already exists.");
		}
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof TaskGroupName that)) {
			return false;
		}
		return Objects.equals(absoluteName(), that.absoluteName()) &&
				Objects.equals(shortName(), that.shortName());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(absoluteName(), shortName());
	}
}
