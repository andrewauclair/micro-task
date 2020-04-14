// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.group.name;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.task.TaskGroupName;
import com.andrewauclair.microtask.task.Tasks;

import java.util.Objects;

public class ExistingTaskGroupName extends TaskGroupName {
	public ExistingTaskGroupName(Tasks tasks, String name) {
		super(tasks, name);

		if (!tasks.hasGroupPath(absoluteName())) {
			throw new TaskException("Group '" + absoluteName() + "' does not exist.");
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
