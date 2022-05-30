// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task.list.name;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.task.TaskListFinder;
import com.andrewauclair.microtask.task.TaskListName;
import com.andrewauclair.microtask.task.Tasks;

import java.util.Objects;

public class NewTaskListName extends TaskListName {
	public NewTaskListName(Tasks tasks, String name) {
		super(tasks, name);

		TaskListFinder finder = new TaskListFinder(tasks);

		if (finder.hasList(this)) {
			throw new TaskException("List '" + absoluteName() + "' already exists.");
		}
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof TaskListName)) {
			return false;
		}
		// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
		TaskListName that = (TaskListName) o;

		return Objects.equals(absoluteName(), that.absoluteName()) &&
				Objects.equals(shortName(), that.shortName()) &&
				Objects.equals(parentGroupName(), that.parentGroupName());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(absoluteName(), shortName(), parentGroupName());
	}
}
