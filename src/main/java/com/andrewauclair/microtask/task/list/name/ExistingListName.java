// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.list.name;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.task.TaskListFinder;
import com.andrewauclair.microtask.task.TaskListName;
import com.andrewauclair.microtask.task.Tasks;

import java.util.Objects;

public class ExistingListName extends TaskListName {
	public ExistingListName(Tasks tasks, String name) {
		super(tasks, name);

		TaskListFinder finder = new TaskListFinder(tasks);

		if (!finder.hasList(this)) {//!tasks.hasListWithName(absoluteName())) {
			throw new TaskException("List '" + absoluteName() + "' does not exist.");
		}
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof TaskListName that)) {
			return false;
		}
		return Objects.equals(absoluteName(), that.absoluteName()) &&
				Objects.equals(shortName(), that.shortName()) &&
				Objects.equals(parentGroupName(), that.parentGroupName());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(absoluteName(), shortName(), parentGroupName());
	}
}
